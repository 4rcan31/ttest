# syntax=docker/dockerfile:1
# Imagen única parametrizada para los tres microservicios:
#   docker build --build-arg SERVICE=user-service -t sportshop/user-service ./backend

# ---- Etapa 1: compilación con Maven ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Primero solo los POM para aprovechar la caché de dependencias entre builds.
COPY pom.xml .
COPY common/pom.xml common/
COPY user-service/pom.xml user-service/
COPY catalog-service/pom.xml catalog-service/
COPY order-service/pom.xml order-service/

COPY common/src common/src
COPY user-service/src user-service/src
COPY catalog-service/src catalog-service/src
COPY order-service/src order-service/src

ARG SERVICE
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q -pl ${SERVICE} -am package -DskipTests \
    && cp ${SERVICE}/target/${SERVICE}.jar /workspace/app.jar

# ---- Etapa 2: imagen de ejecución mínima (solo JRE) ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Usuario sin privilegios: el proceso no corre como root dentro del contenedor.
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build --chown=spring:spring /workspace/app.jar app.jar

# El puerto lo define cada servicio con la variable PORT (8081, 8082, 8083).
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
