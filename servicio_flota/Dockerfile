# Etapa 1: Construir TODO el proyecto
FROM maven:3.9.6-eclipse-temurin-21 as builder
WORKDIR /app

# Copiar el POM padre
COPY pom.xml .

# Copiar los POMs de los módulos
COPY servicio_cliente/pom.xml ./servicio_cliente/
COPY servicio_flota/pom.xml ./servicio_flota/
COPY servicio_logistico/pom.xml ./servicio_logistico/
COPY servicio_tarifa/pom.xml ./servicio_tarifa/

# Descargar dependencias para todo el proyecto
RUN mvn dependency:go-offline -B

# Copiar el código fuente de todo el proyecto
# ¡LÍNEA BORRADA! No hay 'src' en la raíz.
COPY servicio_cliente/src ./servicio_cliente/src
COPY servicio_flota/src ./servicio_flota/src
COPY servicio_logistico/src ./servicio_logistico/src
COPY servicio_tarifa/src ./servicio_tarifa/src

# Construir (compilar) todo el proyecto
RUN mvn package -DskipTests

# ---
# Etapa 2: Crear la imagen final y ligera
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# ¡LA LÍNEA CLAVE! Copiar solo el JAR que necesitamos de la etapa 1
COPY --from=builder /app/servicio_flota/target/*.jar app.jar

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]