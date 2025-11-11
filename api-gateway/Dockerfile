# Plantilla de Dockerfile para servicio-logistica
FROM maven:3.9.6-eclipse-temurin-21 as builder
WORKDIR /app

# 1. Copiar el POM padre
COPY pom.xml .

# 2. Copiar los POMs de TODOS los módulos
COPY servicio_cliente/pom.xml ./servicio_cliente/
COPY servicio_flota/pom.xml ./servicio_flota/
COPY servicio_logistico/pom.xml ./servicio_logistico/
COPY servicio_tarifa/pom.xml ./servicio_tarifa/
COPY api-gateway/pom.xml ./api-gateway/

# 3. Descargar dependencias (basado en los POMs)
RUN mvn dependency:go-offline -B

# 4. Copiar el CÓDIGO FUENTE de TODOS los módulos
COPY servicio_cliente/src ./servicio_cliente/src
COPY servicio_flota/src ./servicio_flota/src
COPY servicio_logistico/src ./servicio_logistico/src
COPY servicio_tarifa/src ./servicio_tarifa/src
COPY api-gateway/src ./api-gateway/src 

# 5. Construir (empaquetar) todo el proyecto
RUN mvn package -DskipTests

# --- Etapa Final: Crear la imagen ligera ---
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# 6. Copiar SÓLO el JAR de este servicio
COPY --from=builder /app/api-gateway/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]