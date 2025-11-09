FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Copiar el POM padre primero
COPY pom.xml ./
COPY servicio_logistico/pom.xml ./servicio_logistico/

# Resolver dependencias
RUN mvn -f servicio_logistico/pom.xml dependency:go-offline -B || true

# Copiar código fuente
COPY servicio_logistico/src ./servicio_logistico/src

# Compilar
RUN mvn -f servicio_logistico/pom.xml clean package -DskipTests

# Etapa de runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN apk add --no-cache curl
COPY --from=build /build/servicio_logistico/target/*.jar app.jar
EXPOSE 8081
ENV SPRING_PROFILES_ACTIVE=prod
ENTRYPOINT ["java", "-jar", "app.jar"]