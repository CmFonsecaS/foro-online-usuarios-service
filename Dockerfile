# ---- Etapa 1: compilar con Maven Wrapper ----
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
# mvnw puede venir con saltos de linea de Windows (CRLF)
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw && ./mvnw -q -B dependency:go-offline
COPY src src
RUN ./mvnw -q -B clean package -DskipTests && cp target/*.jar /app/app.jar

# ---- Etapa 2: imagen liviana solo con el JRE ----
FROM eclipse-temurin:21-jre

WORKDIR /app
# Usuario sin privilegios (UID fijo para leer el wallet montado desde EC2)
RUN useradd --system --uid 10001 --no-create-home appuser \
    && mkdir -p /app/wallet \
    && chown appuser:appuser /app/wallet
COPY --from=build /app/app.jar /app/app.jar

# El wallet de Oracle se monta en /app/wallet (nunca queda dentro de la imagen)
ENV TNS_ADMIN=/app/wallet
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"
USER appuser

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
