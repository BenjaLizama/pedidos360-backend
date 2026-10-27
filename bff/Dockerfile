# ==================================================
# ETAPA 1 - COMPILACIÓN
# ==================================================
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Copiar primero las dependencias para aprovechar la caché
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Copiar el código fuente y compilar
COPY src ./src
RUN mvn -B clean package -DskipTests

# ==================================================
# ETAPA 2 - EJECUCIÓN
# ==================================================
FROM eclipse-temurin:21-jre
WORKDIR /app

# Usuario sin privilegios para ejecutar la aplicación
RUN groupadd --system spring && useradd --system --gid spring spring
COPY --from=build /workspace/target/*.jar /app/app.jar
RUN chown spring:spring /app/app.jar
USER spring
EXPOSE 8083
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]