FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Copiar el descriptor de dependencias.
COPY pom.xml .

# Descargar dependencias con Maven instalado en la imagen.
RUN mvn -B -DskipTests dependency:go-offline

# Copiar el código fuente y compilar.
COPY src ./src

RUN mvn -B -DskipTests clean package

FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

# Crear un usuario sin privilegios.
RUN addgroup -S spring && adduser -S spring -G spring

COPY --from=build /app/target/*.jar app.jar

ENV SERVER_PORT=8081

EXPOSE 8081

USER spring

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]