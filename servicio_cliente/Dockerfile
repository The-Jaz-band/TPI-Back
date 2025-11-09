FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Copiar el POM padre primero
COPY pom.xml ./
COPY servicio_cliente/pom.xml ./servicio_cliente/

# Resolver dependencias del proyecto padre e hijo
RUN mvn -f servicio_cliente/pom.xml dependency:go-offline -B || true

# Copiar código fuente
COPY servicio_cliente/src ./servicio_cliente/src

# Compilar el microservicio
RUN mvn -f servicio_cliente/pom.xml clean package -DskipTests

# Etapa de runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Instalar curl para health checks
RUN apk add --no-cache curl

# Copiar JAR compilado
COPY --from=build /build/servicio_cliente/target/*.jar app.jar

EXPOSE 8082
ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java", "-jar", "app.jar"]