# syntax=docker/dockerfile:1

# ---- build: compila e empacota (as dependencias ficam em camada propria, reaproveitada pelo cache) ----
FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN ./mvnw -B -q dependency:go-offline
COPY src src
RUN ./mvnw -B -q -DskipTests package

# ---- runtime: so o JRE, sem privilegios de root ----
FROM eclipse-temurin:25-jre
RUN useradd --system --uid 10001 --no-create-home aguia
WORKDIR /app
COPY --from=build /workspace/target/aguia-inova-api-*.jar app.jar
USER aguia
EXPOSE 8080
HEALTHCHECK --interval=15s --timeout=5s --start-period=40s --retries=5 \
    CMD curl -fsS http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
