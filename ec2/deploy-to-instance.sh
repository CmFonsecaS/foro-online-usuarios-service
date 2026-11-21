#!/usr/bin/env bash
# =====================================================
# Se ejecuta en GitHub Actions. Envia un script a la EC2
# mediante AWS Systems Manager (SSM), espera el resultado
# y hace una prueba de humo contra la URL publica.
#
# Uso: deploy-to-instance.sh <instance-id> <region> <ruta-prueba> <script-host> [args...]
# =====================================================
set -Eeuo pipefail

instance_id="${1:?ID de la instancia EC2 requerido}"
region="${2:?region requerida}"
smoke_path="${3:?ruta de prueba requerida}"
host_script="${4:?script de despliegue requerido}"
shift 4

if [[ ! "$instance_id" =~ ^i-[0-9a-f]{8,17}$ || ! "$region" =~ ^[a-z0-9-]+$ ]]; then
  echo "ID de instancia o region invalidos." >&2
  exit 2
fi

quote_argument() {
  local value="$1"
  value="${value//\'/\'\\\'\'}"
  printf "'%s'" "$value"
}

script_b64="$(gzip -c "$host_script" | base64 -w0)"
remote_script="/tmp/$(basename "$host_script").${GITHUB_RUN_ID:-local}.sh"
remote_command="set -eu; trap 'rm -f -- $remote_script' EXIT; printf '%s' '$script_b64' | base64 --decode | gzip --decompress > '$remote_script'; chmod 700 '$remote_script'; if bash '$remote_script'"
for argument in "$@"; do
  remote_command+=" $(quote_argument "$argument")"
done
remote_command+="; then estado=0; else estado=\$?; fi; exit \"\$estado\""

parameters_file="$(mktemp)"
trap 'rm -f -- "$parameters_file"' EXIT
jq -n --arg command "$remote_command" '{commands: [$command]}' > "$parameters_file"

command_id="$(aws ssm send-command \
  --instance-ids "$instance_id" \
  --document-name AWS-RunShellScript \
  --comment "Despliegue Foro Online desde GitHub Actions" \
  --timeout-seconds 900 \
  --parameters "file://$parameters_file" \
  --region "$region" \
  --query 'Command.CommandId' \
  --output text)"

for intento in $(seq 1 120); do
  if invocation="$(aws ssm get-command-invocation \
      --command-id "$command_id" \
      --instance-id "$instance_id" \
      --region "$region" \
      --output json 2>/dev/null)"; then
    status="$(jq -r '.Status' <<< "$invocation")"
    case "$status" in
      Success)
        jq -r '.StandardOutputContent | select(length > 0)' <<< "$invocation"
        break
        ;;
      Failed|Cancelled|TimedOut|Cancelling)
        jq -r '.StandardOutputContent, .StandardErrorContent | select(length > 0)' <<< "$invocation" >&2
        echo "El comando de despliegue termino con estado $status." >&2
        exit 1
        ;;
    esac
  fi
  if (( intento == 120 )); then
    echo "Tiempo de espera agotado para el despliegue." >&2
    exit 1
  fi
  sleep 5
done

public_ip="$(aws ec2 describe-instances \
  --instance-ids "$instance_id" \
  --region "$region" \
  --query 'Reservations[0].Instances[0].PublicIpAddress' \
  --output text)"

curl --silent --show-error --retry 12 --retry-all-errors --retry-delay 5 --max-time 10 \
  --output /dev/null --write-out "Prueba de humo: HTTP %{http_code} en http://${public_ip}${smoke_path}\n" \
  "http://${public_ip}${smoke_path}"
echo "URL publica: http://${public_ip}"
