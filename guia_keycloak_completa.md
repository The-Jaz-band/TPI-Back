# 🔐 Guía Completa: Keycloak para el Sistema de Logística

## FASE 1: Instalar Keycloak con Docker

### Paso 1.1: Crear archivo docker-compose-keycloak.yml

Crea este archivo en la raíz de tu proyecto:

```yaml
# docker-compose-keycloak.yml
version: '3.8'

services:
  postgres-keycloak:
    image: postgres:15-alpine
    container_name: postgres-keycloak
    environment:
      POSTGRES_DB: keycloak
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: cjm81238
    volumes:
      - keycloak-db-data:/var/lib/postgresql/data
    networks:
      - keycloak-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 10s
      timeout: 5s
      retries: 5

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
      - keycloak-network
    command: start-dev
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/health/ready"]
      interval: 30s
      timeout: 10s
      retries: 5
      start_period: 60s

networks:
  keycloak-network:
    driver: bridge

volumes:
  keycloak-db-data:
```

### Paso 1.2: Iniciar Keycloak

```bash
# Desde la raíz del proyecto
docker-compose -f docker-compose-keycloak.yml up -d

# Ver logs para verificar que inició correctamente
docker-compose -f docker-compose-keycloak.yml logs -f keycloak
```

**Espera a ver este mensaje:**
```
Keycloak 23.0.0 started in Xms.
```

### Paso 1.3: Acceder a Keycloak

1. Abrir navegador en: **http://localhost:8080**
2. Hacer clic en **"Administration Console"**
3. Login con:
   - **Username:** `admin`
   - **Password:** `admin`

---

## FASE 2: Configurar Realm

### Paso 2.1: Crear Realm

1. En el menú izquierdo, hacer clic en **"master"** (arriba)
2. Click en **"Create Realm"**
3. Configurar:
   - **Realm name:** `logistica-realm`
   - **Enabled:** ON ✅
4. Click **"Create"**

### Paso 2.2: Configurar Realm Settings

1. En el menú izquierdo: **"Realm settings"**
2. Tab **"General"**:
   - **User Profile Enabled:** OFF
   - **Email as username:** OFF
3. Tab **"Login"**:
   - **User registration:** ON ✅ (opcional, para pruebas)
   - **Forgot password:** OFF
   - **Remember me:** ON ✅
4. Click **"Save"**

---

## FASE 3: Crear Roles

### Paso 3.1: Crear Rol CLIENTE

1. Menú izquierdo: **"Realm roles"**
2. Click **"Create role"**
3. Configurar:
   - **Role name:** `CLIENTE`
   - **Description:** `Rol para clientes que solicitan transportes`
4. Click **"Save"**

### Paso 3.2: Crear Rol OPERADOR

1. Click **"Create role"**
2. Configurar:
   - **Role name:** `OPERADOR`
   - **Description:** `Rol para operadores que gestionan el sistema`
3. Click **"Save"**

### Paso 3.3: Crear Rol TRANSPORTISTA

1. Click **"Create role"**
2. Configurar:
   - **Role name:** `TRANSPORTISTA`
   - **Description:** `Rol para transportistas que realizan los traslados`
3. Click **"Save"**

### Verificar:
Deberías ver 3 roles creados:
- ✅ CLIENTE
- ✅ OPERADOR
- ✅ TRANSPORTISTA

---

## FASE 4: Crear Clients (Aplicaciones)

### Paso 4.1: Crear Client para Microservicios

1. Menú izquierdo: **"Clients"**
2. Click **"Create client"**

**General Settings:**
- **Client type:** `OpenID Connect`
- **Client ID:** `logistica-backend`
- Click **"Next"**

**Capability config:**
- **Client authentication:** ON ✅
- **Authorization:** OFF
- **Standard flow:** ON ✅
- **Direct access grants:** ON ✅
- Click **"Next"**

**Login settings:**
- **Root URL:** `http://localhost:8081`
- **Valid redirect URIs:** `*`
- **Valid post logout redirect URIs:** `*`
- **Web origins:** `*`
- Click **"Save"**

### Paso 4.2: Obtener Client Secret

1. En la página del client `logistica-backend`
2. Tab **"Credentials"**
3. **Copiar el "Client secret"** (lo necesitarás después)
   - Ejemplo: `xK8mP3nQ9rT5wY2vL7sB1cF4gH6jN0m`

---

## FASE 5: Crear Usuarios de Prueba

### Paso 5.1: Crear Usuario CLIENTE

1. Menú izquierdo: **"Users"**
2. Click **"Add user"**

**General:**
- **Username:** `cliente1`
- **Email:** `cliente1@example.com`
- **Email verified:** ON ✅
- **First name:** `Juan`
- **Last name:** `Cliente`
- **Enabled:** ON ✅
- Click **"Create"**

**Credentials:**
1. Tab **"Credentials"**
2. Click **"Set password"**
3. Configurar:
   - **Password:** `cliente123`
   - **Password confirmation:** `cliente123`
   - **Temporary:** OFF ⚠️ (IMPORTANTE)
4. Click **"Save"**

**Role Mapping:**
1. Tab **"Role mapping"**
2. Click **"Assign role"**
3. Filtrar roles: Cambiar "Filter by clients" a **"Filter by realm roles"**
4. Seleccionar **"CLIENTE"**
5. Click **"Assign"**

### Paso 5.2: Crear Usuario OPERADOR

Repetir el proceso con:
- **Username:** `operador1`
- **Email:** `operador1@example.com`
- **First name:** `María`
- **Last name:** `Operador`
- **Password:** `operador123`
- **Role:** `OPERADOR`

### Paso 5.3: Crear Usuario TRANSPORTISTA

Repetir el proceso con:
- **Username:** `transportista1`
- **Email:** `transportista1@example.com`
- **First name:** `Carlos`
- **Last name:** `Transportista`
- **Password:** `transportista123`
- **Role:** `TRANSPORTISTA`

### Verificar:
En **"Users"** deberías ver 3 usuarios:
- ✅ cliente1 (rol: CLIENTE)
- ✅ operador1 (rol: OPERADOR)
- ✅ transportista1 (rol: TRANSPORTISTA)

---

## FASE 6: Obtener Token JWT (Prueba)

### Paso 6.1: Obtener Token con cURL

```bash
curl -X POST http://localhost:8080/realms/logistica-realm/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=operador1" \
  -d "password=operador123" \
  -d "grant_type=password" \
  -d "client_id=logistica-backend" \
  -d "client_secret=TU_CLIENT_SECRET_AQUI"
```

**Reemplazar:** `TU_CLIENT_SECRET_AQUI` con el secret que copiaste en el Paso 4.2

### Paso 6.2: Respuesta Esperada

```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expires_in": 300,
  "refresh_expires_in": 1800,
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",
  "not-before-policy": 0,
  "session_state": "...",
  "scope": "profile email"
}
```

### Paso 6.3: Verificar el Token

Copia el `access_token` y pégalo en: **https://jwt.io**

Deberías ver en el payload:
```json
{
  "realm_access": {
    "roles": [
      "OPERADOR",
      "default-roles-logistica-realm",
      "offline_access",
      "uma_authorization"
    ]
  },
  "preferred_username": "operador1",
  "email": "operador1@example.com"
}
```

---

## FASE 7: Configurar Microservicios para Usar Keycloak

### Paso 7.1: Actualizar application.yaml

**Para CADA microservicio**, actualizar:

```yaml
spring:
  profiles:
    active: prod  # ← Cambiar de "dev" a "prod" para habilitar seguridad
  
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8080/realms/logistica-realm
          jwk-set-uri: http://localhost:8080/realms/logistica-realm/protocol/openid-connect/certs
```

### Paso 7.2: Eliminar o Comentar SecurityConfigDev

Si creaste `SecurityConfigDev.java` para pruebas, ahora debes:

**Opción A:** Eliminarlo
```bash
rm src/main/java/com/grupo81/.../config/SecurityConfigDev.java
```

**Opción B:** Cambiar el profile a "dev" (para que no se active)
```java
@Configuration
@EnableWebSecurity
@Profile("dev")  // Solo se activa con profile "dev"
public class SecurityConfigDev {
    // ...
}
```

### Paso 7.3: Usar SecurityConfig real

Asegúrate de tener el `SecurityConfig.java` que te pasé antes con la configuración JWT.

---

## FASE 8: Probar con Swagger + Keycloak

### Paso 8.1: Reiniciar Microservicios

```bash
# Terminal 1
cd servicio_cliente
mvn spring-boot:run

# Terminal 2
cd servicio_flota
mvn spring-boot:run

# Terminal 3
cd servicio_tarifa
mvn spring-boot:run

# Terminal 4
cd servicio_logistico
mvn spring-boot:run
```

### Paso 8.2: Obtener Token

```bash
curl -X POST http://localhost:8080/realms/logistica-realm/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=operador1" \
  -d "password=operador123" \
  -d "grant_type=password" \
  -d "client_id=logistica-backend" \
  -d "client_secret=TU_CLIENT_SECRET"
```

Copiar el `access_token`.

### Paso 8.3: Usar Token en Swagger

1. Abrir: http://localhost:8081/swagger-ui.html
2. Click en **"Authorize"** (candado verde arriba a la derecha)
3. En el campo **"Value"**, pegar:
   ```
   Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...
   ```
   (⚠️ Importante: incluir la palabra "Bearer" seguida de un espacio)
4. Click **"Authorize"**
5. Click **"Close"**

### Paso 8.4: Probar Endpoint Protegido

1. En Swagger, expandir cualquier endpoint
2. Click **"Try it out"**
3. Llenar los datos necesarios
4. Click **"Execute"**

Deberías ver una respuesta exitosa (200, 201, etc.) en lugar de 401 Unauthorized.

---

## FASE 9: Scripts Útiles para Obtener Tokens

### Script Bash (Linux/Mac)

Crear `get-token.sh`:

```bash
#!/bin/bash

# Configuración
KEYCLOAK_URL="http://localhost:8080"
REALM="logistica-realm"
CLIENT_ID="logistica-backend"
CLIENT_SECRET="TU_CLIENT_SECRET_AQUI"

# Usuario y contraseña (pasar como argumentos)
USERNAME=${1:-operador1}
PASSWORD=${2:-operador123}

# Obtener token
TOKEN_RESPONSE=$(curl -s -X POST \
  "${KEYCLOAK_URL}/realms/${REALM}/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=${USERNAME}" \
  -d "password=${PASSWORD}" \
  -d "grant_type=password" \
  -d "client_id=${CLIENT_ID}" \
  -d "client_secret=${CLIENT_SECRET}")

# Extraer access_token
ACCESS_TOKEN=$(echo $TOKEN_RESPONSE | jq -r '.access_token')

echo "Token obtenido:"
echo "Bearer $ACCESS_TOKEN"
```

Uso:
```bash
chmod +x get-token.sh
./get-token.sh operador1 operador123
```

### Script PowerShell (Windows)

Crear `get-token.ps1`:

```powershell
param(
    [string]$Username = "operador1",
    [string]$Password = "operador123"
)

$keycloakUrl = "http://localhost:8080"
$realm = "logistica-realm"
$clientId = "logistica-backend"
$clientSecret = "TU_CLIENT_SECRET_AQUI"

$body = @{
    username = $Username
    password = $Password
    grant_type = "password"
    client_id = $clientId
    client_secret = $clientSecret
}

$response = Invoke-RestMethod -Uri "$keycloakUrl/realms/$realm/protocol/openid-connect/token" `
    -Method Post `
    -ContentType "application/x-www-form-urlencoded" `
    -Body $body

Write-Host "Token obtenido:"
Write-Host "Bearer $($response.access_token)"
```

Uso:
```powershell
.\get-token.ps1 -Username operador1 -Password operador123
```

---

## FASE 10: Troubleshooting Keycloak

### Problema: "401 Unauthorized" en Swagger

**Causa:** Token inválido o expirado

**Solución:**
1. Obtener un nuevo token (expiran en 5 minutos por defecto)
2. Verificar que incluiste "Bearer " antes del token
3. Verificar que el usuario tiene el rol correcto

### Problema: "403 Forbidden"

**Causa:** Usuario no tiene el rol necesario

**Solución:**
1. En Keycloak, ir a Users → [usuario] → Role mapping
2. Verificar que tiene el rol correcto asignado
3. Obtener un nuevo token después de asignar el rol

### Problema: "Cannot connect to Keycloak"

**Causa:** Keycloak no está corriendo

**Solución:**
```bash
docker-compose -f docker-compose-keycloak.yml ps
# Si no está corriendo:
docker-compose -f docker-compose-keycloak.yml up -d
```

### Problema: SecurityConfig no funciona

**Causa:** Profile incorrecto en application.yaml

**Solución:**
```yaml
spring:
  profiles:
    active: prod  # ← Debe ser "prod", no "dev"
```

---

## 📊 Resumen de Configuración

| Item | Valor |
|------|-------|
| **Keycloak URL** | http://localhost:8080 |
| **Admin User** | admin / admin |
| **Realm** | logistica-realm |
| **Client ID** | logistica-backend |
| **Users** | cliente1, operador1, transportista1 |
| **Passwords** | cliente123, operador123, transportista123 |
| **Roles** | CLIENTE, OPERADOR, TRANSPORTISTA |

---

## ✅ Checklist Final

- [ ] Keycloak está corriendo en puerto 8080
- [ ] Realm "logistica-realm" creado
- [ ] 3 roles creados: CLIENTE, OPERADOR, TRANSPORTISTA
- [ ] Client "logistica-backend" creado
- [ ] Client Secret guardado
- [ ] 3 usuarios creados con sus roles
- [ ] Puedo obtener un token con cURL
- [ ] El token tiene los roles correctos (verificado en jwt.io)
- [ ] Microservicios configurados con spring.profiles.active=prod
- [ ] SecurityConfig tiene la configuración JWT correcta
- [ ] Puedo hacer requests en Swagger con el token

---

¿Listo para continuar con Docker? 🐳