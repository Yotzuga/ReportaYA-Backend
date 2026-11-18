# Imagen del backend ReportaYA para Render (o cualquier host con Docker).
# Etapa 1: compila el jar con Maven. Etapa 2: solo el JRE + el jar.

FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
# Primero el pom para cachear dependencias entre despliegues.
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B -DskipTests package && cp target/*.jar /app/app.jar

FROM eclipse-temurin:17-jre
WORKDIR /app
RUN useradd --system --uid 10001 reportaya
COPY --from=build /app/app.jar /app/app.jar
COPY docker-entrypoint.sh /app/docker-entrypoint.sh
RUN chmod 0755 /app/docker-entrypoint.sh
USER reportaya

# Memoria acotada para el plan gratis de Render (512 MB) y arranque mas rapido.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70 -XX:+UseSerialGC -Xss512k -XX:TieredStopAtLevel=1"
EXPOSE 8081
ENTRYPOINT ["/app/docker-entrypoint.sh"]
