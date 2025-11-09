# 🐳 Guía Completa: Dockerizar Sistema de Microservicios

## FASE 1: Crear Dockerfiles

### Paso 1.1: Dockerfile para Servicio Cliente

Crear `servicio_cliente/Dockerfile`:

```dockerfile
# Etapa 1: Build
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copiar archivos de configuración Maven
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar código fuente y compilar
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copiar JAR compilado
COPY --from=build /app/target/*.jar app.jar

# Exponer puerto
EXPOSE 8082

# Variables de entorno por defecto (se pueden sobreescribir)
ENV SPRING_PROFILES_ACTIVE=prod

# Comando de inicio
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Paso 1.2: Dockerfile para Servicio Flota

Crear `servicio_flota/Dockerfile`:

```dockerfile
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8083
ENV SPRING_PROFILES_ACTIVE=prod
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Paso 1.3: Dockerfile para Servicio Tarifa

Crear `servicio_tarifa/Dockerfile`:

```dockerfile
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8084
ENV SPRING_PROFILES_ACTIVE=prod
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Paso 1.4: Dockerfile para Servicio Logística

Crear `servicio_logistico/Dockerfile`:

```dockerfile
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8081
ENV SPRING_PROFILES_ACTIVE=prod
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## FASE 2: Crear docker-compose.yml Completo

Crear `docker-compose.yml` en la raíz del proyecto:

```yaml
version: '3.8'

services:
  # ==================== BASES DE DATOS ====================
  
  postgres-logistica:
    image: postgres:15-alpine
    container_name: postgres-logistica
    environment:
      POSTGRES_DB: logistica_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: cjm81238
    ports:
      - "5432:5432"
    volumes:
      - postgres-logistica-data:/var/lib/postgresql/data
    networks:
      - logistica-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 10s
      timeout: 5s
      retries: 5

  postgres-cliente:
    image: postgres:15-alpine
    container_name: postgres-cliente
    environment:
      POSTGRES_DB: cliente_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: cjm81238
    ports:
      - "5433:5432"
    volumes:
      - postgres-cliente-data:/var/lib/postgresql/data
    networks:
      - logistica-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 10s
      timeout: 5s
      retries: 5

  postgres-flota:
    image: postgres:15-alpine
    container_name: postgres-flota
    environment:
      POSTGRES_DB: flota_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: cjm81238
    ports:
      - "5434:5432"
    volumes:
      - postgres-flota-data:/var/lib/postgresql/data
    networks:
      - logistica-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 10s
      timeout: 5s
      retries: 5

  postgres-tarifa:
    image: postgres:15-alpine
    container_name: postgres-tarifa
    environment:
      POSTGRES_DB: tarifa_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: cjm81238
    ports:
      - "5435:5432"
    volumes:
      - postgres-tarifa-data:/var/lib/postgresql/data
    networks:
      - logistica-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 10s
      timeout: 5s
      retries: 5

  postgres-keycloak:
    image: postgres:15-alpine
    container_name: postgres-keycloak
    environment:
      POSTGRES_DB: keycloak
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: cjm81238
    volumes:
      - postgres-keycloak-data:/var/lib/postgresql/data
    networks:
      - logistica-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 10s
      timeout: 5s
      retries: 5

  # ==================== KEYCLOAK ====================
  
  keycloak:
    image: quay.io/keycloak/keycloak:23.0
    container_name: keycloak
    environment:
      KEYCLOAK_ADMIN: admin
      KEYCLOAK_ADMIN_PASSWORD: admin
      KC_DB: postgres
      KC_DB_URL: jdbc:postgresql://postgres-keycloak:5432/keycloak
      KC_DB_USERNAME: postgres
      KC_DB_PASSWORD: cjm81238
      KC_HOSTNAME_STRICT: false
      KC_HTTP_ENABLED: true
      KC_HOSTNAME_STRICT_HTTPS: false
    ports:
      - "8080:8080"
    depends_on:
      postgres-keycloak:
        condition: service_healthy
    networks:
      - logistica-network
    command: start-dev
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/health/ready"]
      interval: 30s
      timeout: 10s
      retries: 5
      start_period: 90s

  # ==================== MICROSERVICIOS ====================
  
  servicio-cliente:
    build:
      context: ./servicio_cliente
      dockerfile: Dockerfile
    container_name: servicio-cliente
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres-cliente:5432/cliente_db
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: cjm81238
      SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI: http://keycloak:8080/realms/logistica-realm
      SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI: http://keycloak:8080/realms/logistica-realm/protocol/openid-connect/certs
    ports:
      - "8082:8082"
    depends_on:
      postgres-cliente:
        condition: service_healthy
      keycloak:
        condition: service_healthy
    networks:
      - logistica-network
    restart: on-failure
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8082/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s

  servicio-flota:
    build:
      context: ./servicio_flota
      dockerfile: Dockerfile
    container_name: servicio-flota
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres-flota:5432/flota_db
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: cjm81238
      SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI: http://keycloak:8080/realms/logistica-realm
      SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI: http://keycloak:8080/realms/logistica-realm/protocol/openid-connect/certs
    ports:
      - "8083:8083"
    depends_on:
      postgres-flota:
        condition: service_healthy
      keycloak:
        condition: service_healthy
    networks:
      - logistica-network
    restart: on-failure
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8083/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s

  servicio-tarifa:
    build:
      context: ./servicio_tarifa
      dockerfile: Dockerfile
    container_name: servicio-tarifa
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres-tarifa:5432/tarifa_db
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: cjm81238
      SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI: http://keycloak:8080/realms/logistica-realm
      SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI: http://keycloak:8080/realms/logistica-realm/protocol/openid-connect/certs
    ports:
      - "8084:8084"
    depends_on:
      postgres-tarifa:
        condition: service_healthy
      keycloak:
        condition: service_healthy
    networks:
      - logistica-network
    restart: on-failure
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8084/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s

  servicio-logistica:
    build:
      context: ./servicio_logistico
      dockerfile: Dockerfile
    container_name: servicio-logistica
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres-logistica:5432/logistica_db
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: cjm81238
      SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI: http://keycloak:8080/realms/logistica-realm
      SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI: http://keycloak:8080/realms/logistica-realm/protocol/openid-connect/certs
      MICROSERVICES_CLIENTES_URL: http://servicio-cliente:8082
      MICROSERVICES_FLOTA_URL: http://servicio-flota:8083
      MICROSERVICES_TARIFAS_URL: http://servicio-tarifa:8084
      GOOGLE_MAPS_API_KEY: ${GOOGLE_MAPS_API_KEY:-AIzaSyDummy_Key}
    ports:
      - "8081:8081"
    depends_on:
      postgres-logistica:
        condition: service_healthy
      keycloak:
        condition: service_healthy
      servicio-cliente:
        condition: service_healthy
      servicio-flota:
        condition: service_healthy
      servicio-tarifa:
        condition: service_healthy
    networks:
      - logistica-network
    restart: on-failure
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8081/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 90s

networks:
  logistica-network:
    driver: bridge

volumes:
  postgres-logistica-data:
  postgres-cliente-data:
  postgres-flota-data:
  postgres-tarifa-data:
  postgres-keycloak-data:
```

---

## FASE 3: Crear .dockerignore

Crear `.dockerignore` en CADA carpeta de microservicio:

```
# servicio_cliente/.dockerignore
# servicio_flota/.dockerignore
# servicio_tarifa/.dockerignore
# servicio_logistico/.dockerignore

target/
!target/*.jar
.mvn/
mvnw
mvnw.cmd
.git/
.idea/
*.iml
.vscode/
*.log
```

---

## FASE 4: Construir y Ejecutar

### Paso 4.1: Detener Servicios Locales

Detener todos los microservicios que tengas corriendo localmente:
```bash
# Presionar Ctrl+C en cada terminal donde estén corriendo
```

### Paso 4.2: Construir Imágenes

```bash
# Desde la raíz del proyecto
docker-compose build

# Esto tomará 5-10 minutos la primera vez
```

### Paso 4.3: Iniciar Todo el Sistema

```bash
docker-compose up -d

# Ver el progreso
docker-compose logs -f
```

### Paso 4.4: Verificar que Todo Está Corriendo

```bash
docker-compose ps
```

Deberías ver:
```
NAME                  STATUS
keycloak              Up (healthy)
postgres-keycloak     Up (healthy)
postgres-logistica    Up (healthy)
postgres-cliente      Up (healthy)
postgres-flota        Up (healthy)
postgres-tarifa       Up (healthy)
servicio-cliente      Up (healthy)
servicio-flota        Up (healthy)
servicio-tarifa       Up (healthy)
servicio-logistica    Up (healthy)
```

---

## FASE 5: Configurar Keycloak en Docker

### Paso 5.1: Acceder a Keycloak

1. Abrir: http://localhost:8080
2. Login con admin/admin
3. **Seguir TODA la FASE 2, 3, 4 y 5 de la Guía de Keycloak** (crear realm, roles, users, etc.)

---

## FASE 6: Probar el Sistema

### Paso 6.1: Obtener Token

```bash
curl -X POST http://localhost:8080/realms/logistica-realm/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=operador1" \
  -d "password=operador123" \
  -d "grant_type=password" \
  -d "client_id=logistica-backend" \
  -d "client_secret=TU_CLIENT_SECRET"
```

### Paso 6.2: Probar Swagger

1. http://localhost:8081/swagger-ui.html
2. Autorizar con el token
3. Probar endpoints

### Paso 6.3: Crear Datos de Prueba

```bash
# 1. Crear cliente
curl -X POST http://localhost:8082/api/clientes \
  -H "Authorization: Bearer TU_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Juan Pérez",
    "email": "juan@example.com",
    "telefono": "+5491123456789",
    "empresa": "Constructora ABC"
  }'

# 2. Crear camión
curl -X POST http://localhost:8083/api/camiones \
  -H "Authorization: Bearer TU_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "dominio": "ABC123",
    "nombreTransportista": "Carlos Rodríguez",
    "telefonoTransportista": "+5491198765432",
    "capacidadPesoKg": 5000,
    "capacidadVolumenM3": 30,
    "costoBaseKm": 8.50,
    "consumoCombustibleLKm": 0.35
  }'

# 3. Crear tarifas
curl -X POST http://localhost:8084/api/tarifas \
  -H "Authorization: Bearer TU_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "codigoTarifa": "BASE_KM",
    "descripcion": "Costo base por kilómetro",
    "valor": 5.0,
    "unidad": "POR_KM"
  }'
```

---

## FASE 7: Comandos Útiles de Docker

### Ver Logs de un Servicio Específico

```bash
# Logs de servicio-logistica
docker-compose logs -f servicio-logistica

# Últimas 100 líneas
docker-compose logs --tail=100 servicio-logistica

# Logs de todos
docker-compose logs -f
```

### Reiniciar un Servicio

```bash
docker-compose restart servicio-logistica
```

### Rebuild un Servicio Específico

```bash
# Si cambiaste código
docker-compose build servicio-logistica
docker-compose up -d servicio-logistica
```

### Detener Todo

```bash
docker-compose down
```

### Detener y Eliminar Volúmenes (⚠️ Borra BDs)

```bash
docker-compose down -v
```

### Ver Recursos Usados

```bash
docker stats
```

### Entrar a un Contenedor

```bash
# Shell en servicio-logistica
docker exec -it servicio-logistica sh

# Dentro del contenedor
ls -la
env | grep SPRING
exit
```

---

## FASE 8: Troubleshooting Docker

### Problema: "Container exits immediately"

**Ver por qué falló:**
```bash
docker-compose logs servicio-logistica
```

**Causas comunes:**
- Error de compilación (revisar el Dockerfile)
- Base de datos no lista (esperar más tiempo)
- Variables de entorno incorrectas

### Problema: "Cannot connect to database"

**Verificar red:**
```bash
docker network inspect sistema-logistica_logistica-network
```

**Verificar que PostgreSQL está healthy:**
```bash
docker-compose ps postgres-logistica
```

### Problema: "Port already in use"

**Ver qué está usando el puerto:**
```bash
# Windows
netstat -ano | findstr :8081

# Linux/Mac
lsof -i :8081
```

**Matar el proceso o cambiar el puerto en docker-compose.yml**

### Problema: "Build muy lento"

**Usar caché de Maven:**
Editar Dockerfile:
```dockerfile
# Agregar volumen para .m2
# En docker-compose.yml:
volumes:
  - ~/.m2:/root/.m2
```

---

## FASE 9: Script de Inicio Completo

Crear `start-system.sh`:

```bash
#!/bin/bash

echo "🚀 Iniciando Sistema de Logística..."

# Detener contenedores existentes
echo "📦 Deteniendo contenedores existentes..."
docker-compose down

# Construir imágenes
echo "🔨 Construyendo imágenes..."
docker-compose build --no-cache

# Iniciar servicios
echo "🐳 Iniciando servicios..."
docker-compose up -d

# Esperar a que los servicios estén listos
echo "⏳ Esperando a que los servicios estén listos..."
sleep 60

# Verificar estado
echo "✅ Estado de los servicios:"
docker-compose ps

echo ""
echo "🎉 Sistema iniciado!"
echo "📚 Swagger URLs:"
echo "  - Cliente:    http://localhost:8082/swagger-ui.html"
echo "  - Flota:      http://localhost:8083/swagger-ui.html"
echo "  - Tarifa:     http://localhost:8084/swagger-ui.html"
echo "  - Logística:  http://localhost:8081/swagger-ui.html"
echo "🔐 Keycloak:     http://localhost:8080"
echo ""
echo "📋 Ver logs: docker-compose logs -f"
```

Uso:
```bash
chmod +x start-system.sh
./start-system.sh
```

---

## ✅ Checklist Final de Docker

- [ ] Dockerfile creado en cada microservicio
- [ ] .dockerignore creado en cada microservicio
- [ ] docker-compose.yml en la raíz
- [ ] `docker-compose build` ejecutado sin errores
- [ ] `docker-compose up -d` ejecutado
- [ ] Todos los contenedores muestran estado "Up (healthy)"
- [ ] Keycloak accesible en http://localhost:8080
- [ ] Realm y usuarios configurados en Keycloak
- [ ] Puedo obtener un token JWT
- [ ] Swagger accesible en cada microservicio
- [ ] Puedo hacer requests con el token

---

## 📊 URLs del Sistema Dockerizado

| Servicio | URL | Credenciales |
|----------|-----|--------------|
| Keycloak Admin | http://localhost:8080 | admin / admin |
| Servicio Logística | http://localhost:8081/swagger-ui.html | Token JWT |
| Servicio Cliente | http://localhost:8082/swagger-ui.html | Token JWT |
| Servicio Flota | http://localhost:8083/swagger-ui.html | Token JWT |
| Servicio Tarifa | http://localhost:8084/swagger-ui.html | Token JWT |

---

¿Listo para empezar? Sigue los pasos en orden y cualquier error que tengas, compártelo y te ayudo a resolverlo 🚀