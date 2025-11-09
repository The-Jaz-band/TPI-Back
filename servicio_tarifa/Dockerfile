FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Copiar el POM padre primero
COPY pom.xml ./
COPY servicio_tarifa/pom.xml ./servicio_tarifa/

# Resolver dependencias
RUN mvn -f servicio_tarifa/pom.xml dependency:go-offline -B || true

# Copiar código fuente
COPY servicio_tarifa/src ./servicio_tarifa/src

# Compilar
RUN mvn -f servicio_tarifa/pom.xml clean package -DskipTests

# Etapa de runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN apk add --no-cache curl
COPY --from=build /build/servicio_tarifa/target/*.jar app.jar
EXPOSE 8084
ENV SPRING_PROFILES_ACTIVE=prod
ENTRYPOINT ["java", "-jar", "app.jar"]