#!/usr/bin/env bash
# =====================================================
# Se ejecuta DENTRO de la EC2 (via AWS Systems Manager).
# Descarga la imagen desde ECR, prepara el wallet de Oracle
# desde S3, lee las credenciales desde Secrets Manager y
# reemplaza el contenedor del microservicio.
# Si el nuevo contenedor no queda sano, restaura el anterior.
# =====================================================
set -Eeuo pipefail

name="${1:?nombre del servicio requerido}"
port="${2:?puerto requerido}"
image="${3:?URI de la imagen requerida}"
region="${4:?region requerida}"
secret_arn="${5:?ARN del secreto requerido}"
wallet_bucket="${6:?bucket del wallet requerido}"
wallet_key="${7:?clave del wallet requerida}"

if [[ ! "$name" =~ ^[a-z0-9-]+$ || ! "$port" =~ ^[0-9]{4,5}$ ]]; then
  echo "Nombre o puerto invalido." >&2
  exit 2
fi
if [[ ! "$image" =~ ^[0-9]{12}\.dkr\.ecr\.[a-z0-9-]+\.amazonaws\.com/[a-z0-9._/-]+:[A-Za-z0-9._-]+$ ]]; then
  echo "URI de imagen ECR invalida." >&2
  exit 2
fi

wallet_dir=/opt/foro/wallet
registry="${image%%/*}"
docker_config="$(mktemp -d /tmp/foro-ecr.XXXXXX)"
env_file="$(mktemp /tmp/foro-env.XXXXXX)"
export DOCKER_CONFIG="$docker_config"
cleanup() {
  rm -rf -- "$docker_config" "$env_file"
}
trap cleanup EXIT

# 1. Descargar la imagen desde ECR
aws ecr get-login-password --region "$region" \
  | docker login --username AWS --password-stdin "$registry" >/dev/null
docker pull "$image"
docker logout "$registry" >/dev/null 2>&1 || true

# 2. Preparar el wallet de Oracle (compartido por los microservicios)
mkdir -p "$wallet_dir"
wallet_tmp="$(mktemp -d /tmp/foro-wallet.XXXXXX)"
aws s3 cp "s3://${wallet_bucket}/${wallet_key}" "$wallet_tmp/wallet.zip" --region "$region" --only-show-errors
unzip -o -q "$wallet_tmp/wallet.zip" -d "$wallet_tmp/contenido"
if [[ ! -f "$wallet_tmp/contenido/tnsnames.ora" ]]; then
  echo "El ZIP del wallet no contiene tnsnames.ora en su raiz." >&2
  rm -rf -- "$wallet_tmp"
  exit 1
fi
rm -rf -- "${wallet_dir:?}"/*
cp -a "$wallet_tmp/contenido/." "$wallet_dir/"
rm -rf -- "$wallet_tmp"
chown -R 10001:10001 "$wallet_dir"
chmod 700 "$wallet_dir"

# 3. Credenciales desde Secrets Manager -> archivo de entorno temporal
aws secretsmanager get-secret-value \
  --secret-id "$secret_arn" \
  --query SecretString \
  --output text \
  --region "$region" \
  | python3 -c '
import json, sys
secret = json.load(sys.stdin)
with open(sys.argv[1], "w", encoding="utf-8") as out:
    for key in ("DB_URL", "DB_USERNAME", "DB_PASSWORD", "JWT_SECRET"):
        value = secret.get(key)
        if not isinstance(value, str) or not value or "\n" in value:
            raise SystemExit(f"Al secreto le falta un valor valido para {key}.")
        if key == "DB_URL" and not value.startswith("jdbc:"):
            value = f"jdbc:oracle:thin:@{value}?TNS_ADMIN=/app/wallet"
        out.write(f"{key}={value}\n")
' "$env_file"
printf 'CORS_ORIGINS=*\nJAVA_OPTS=-Xmx400m\n' >> "$env_file"
chmod 600 "$env_file"

# 4. Reemplazar el contenedor (con respaldo para rollback)
backup=""
nuevo=false
rollback() {
  status=$?
  trap - EXIT
  if (( status != 0 )); then
    if [[ "$nuevo" == true ]]; then
      docker logs --tail 80 "$name" 2>&1 || true
      docker rm -f "$name" >/dev/null 2>&1 || true
    fi
    if [[ -n "$backup" ]] && docker inspect "$backup" >/dev/null 2>&1; then
      docker rename "$backup" "$name"
      docker start "$name" >/dev/null
      echo "Despliegue fallido: se restauro el contenedor anterior." >&2
    fi
  fi
  cleanup
  exit "$status"
}
trap rollback EXIT

if docker inspect "$name" >/dev/null 2>&1; then
  backup="${name}-respaldo-$(date +%s)"
  docker rename "$name" "$backup"
  docker stop "$backup" >/dev/null || true
fi

nuevo=true
docker run -d \
  --name "$name" \
  --restart unless-stopped \
  --publish "127.0.0.1:${port}:${port}" \
  --env-file "$env_file" \
  --volume "${wallet_dir}:/app/wallet:ro" \
  "$image" >/dev/null
rm -f -- "$env_file"

# 5. Health check: el servicio y su conexion a Oracle deben estar UP
sano=false
for intento in $(seq 1 60); do
  if curl --fail --silent --max-time 5 "http://127.0.0.1:${port}/actuator/health" | grep -q '"status":"UP"'; then
    sano=true
    break
  fi
  sleep 5
done
if [[ "$sano" != true ]]; then
  echo "El servicio ${name} no respondio UP en /actuator/health." >&2
  exit 1
fi

if [[ -n "$backup" ]]; then
  docker rm "$backup" >/dev/null
fi
backup=""
docker image prune -f >/dev/null 2>&1 || true
trap - EXIT
cleanup
echo "Servicio ${name} desplegado correctamente: ${image}"
