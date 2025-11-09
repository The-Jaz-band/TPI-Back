FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Copiar el POM padre primero
COPY pom.xml ./
COPY servicio_flota/pom.xml ./servicio_flota/

# Resolver dependencias
RUN mvn -f servicio_flota/pom.xml dependency:go-offline -B || true

# Copiar código fuente
COPY servicio_flota/src ./servicio_flota/src

# Compilar
RUN mvn -f servicio_flota/pom.xml clean package -DskipTests

# Etapa de runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN apk add --no-cache curl
COPY --from=build /build/servicio_flota/target/*.jar app.jar
EXPOSE 8083
ENV SPRING_PROFILES_ACTIVE=prod
ENTRYPOINT ["java", "-jar", "app.jar"]