# 📚 Guía Completa del Sistema de Logística de Transporte

**Fecha de creación:** Noviembre 2025  
**Versión del proyecto:** 0.0.1-SNAPSHOT  
**Java:** 21 | **Spring Boot:** 3.5.7 | **Maven:** 3.9+

---

## 📑 Índice de Contenidos

1. [Visión General del Sistema](#visión-general-del-sistema)
2. [Arquitectura de Microservicios](#arquitectura-de-microservicios)
3. [Tecnologías Utilizadas](#tecnologías-utilizadas)
4. [Estructura del Proyecto](#estructura-del-proyecto)
5. [Requisitos Previos](#requisitos-previos)
6. [Comandos Esenciales](#comandos-esenciales)
7. [Configuración Inicial](#configuración-inicial)
8. [Cómo Ejecutar el Sistema](#cómo-ejecutar-el-sistema)
9. [Microservicios Detallados](#microservicios-detallados)
10. [API y Endpoints](#api-y-endpoints)
11. [Autenticación y Seguridad](#autenticación-y-seguridad)
12. [Troubleshooting](#troubleshooting)

---

## 🎯 Visión General del Sistema

### ¿Qué es este proyecto?

Es un **sistema de logística de transporte** diseñado para gestionar:

- 📦 **Solicitudes de transporte** de contenedores marítimos
- 🛣️ **Planificación de rutas** con depósitos intermedios
- 🚚 **Asignación de flota** (camiones disponibles)
- 💰 **Cálculo de costos** y tarifas
- 📍 **Seguimiento de ubicaciones** mediante Google Maps API
- 🔐 **Control de acceso** con autenticación JWT (Keycloak)

### Problema que resuelve

Empresas de logística necesitan:
- Registrar solicitudes de traslado de contenedores
- Planificar rutas óptimas
- Asignar recursos (camiones) disponibles
- Calcular costos reales
- Seguimiento del estado en tiempo real

### Arquitectura: Microservicios

En lugar de una aplicación monolítica, el sistema se divide en **4 microservicios independientes** que se comunican entre sí:

```
┌─────────────────────────────────────────────────────────┐
│           CLIENTE (Frontend/Postman)                     │
└────────────────────┬────────────────────────────────────┘
                     │
         ┌───────────┴───────────┐
         │                       │
    ┌────▼─────┐          ┌─────▼────┐
    │ Keycloak  │          │ Servicios │
    │ (Auth)    │          │  (API)    │
    └───────────┘          └─────┬────┘
                                 │
         ┌───────────┬───────────┼───────────┬───────────┐
         │           │           │           │           │
    ┌────▼────┐ ┌───▼───┐ ┌────▼────┐ ┌───▼────┐ ┌───▼────┐
    │ Logística│ │Cliente│ │  Flota  │ │Tarifa  │ │GeoAPI  │
    │ (8081)   │ │(8082) │ │ (8083)  │ │(8084)  │ │(8090)  │
    └────┬────┘ └───┬───┘ └────┬────┘ └───┬────┘ └───┬────┘
         │           │          │          │          │
    ┌────▼────┬─────▼────┬─────▼────┬─────▼────┬─────▼────┐
    │ PostgreSQL PostgreSQL PostgreSQL PostgreSQL   API    │
    │ (5432)     (5433)    (5434)    (5435)    Google Maps│
    └──────────────────────────────────────────────────────┘
```

---

## 🏗️ Arquitectura de Microservicios

### 1. **Servicio de Logística** (Puerto 8081) - El Orquestador

**Responsabilidad principal:** Gestionar todo el proceso de transporte

**Funciones:**
- ✅ Crear y gestionar solicitudes de transporte
- ✅ Planificar rutas con tramos
- ✅ Asignar camiones a tramos
- ✅ Calcular costos (aproximado y final)
- ✅ Coordinar con otros microservicios
- ✅ Seguimiento de contenedores

**Entidades principales:**
- `Solicitud`: Solicitud de transporte de un cliente
- `Contenedor`: Lo que se transporta
- `Ruta`: Plan completo de viaje
- `Tramo`: Segmento individual de una ruta
- `Deposito`: Punto de almacenamiento temporal

---

### 2. **Servicio de Cliente** (Puerto 8082)

**Responsabilidad:** Gestionar datos de clientes

**Funciones:**
- ✅ Registrar clientes
- ✅ Buscar cliente por email/ID
- ✅ Actualizar datos de cliente

**Entidad:**
- `Cliente`: Nombre, email, teléfono, empresa

---

### 3. **Servicio de Flota** (Puerto 8083)

**Responsabilidad:** Administrar camiones disponibles

**Funciones:**
- ✅ Registrar camiones
- ✅ Consultar disponibilidad
- ✅ Buscar camiones por capacidad
- ✅ Actualizar estado

**Entidad:**
- `Camion`: Dominio, capacidades, transportista

---

### 4. **Servicio de Tarifa** (Puerto 8084)

**Responsabilidad:** Calcular costos y tarifas

**Funciones:**
- ✅ Gestionar tarifas base
- ✅ Calcular costos de transporte
- ✅ Configurar precios
- ✅ Cálculo de combustible y estadías

**Entidad:**
- `Tarifa`: Parámetros de precios

---

### 5. **GeoAPI** (Puerto 8090) - Servicio Auxiliar

**Responsabilidad:** Calcular distancias y tiempos

**Funciones:**
- ✅ Integración con Google Maps Distance Matrix
- ✅ Cálculo de distancias entre dos ubicaciones
- ✅ Estimación de tiempos de viaje

**Características:**
- Encapsula la lógica de geolocalización
- Puede ser usado por otros servicios
- Reduce duplicación de código

---

## 🛠️ Tecnologías Utilizadas

| Tecnología | Versión | Propósito |
|------------|---------|----------|
| **Java** | 21 | Lenguaje de programación |
| **Spring Boot** | 3.5.7 | Framework principal |
| **Spring Cloud** | 2025.0.0 | Comunicación entre servicios |
| **PostgreSQL** | 15 | Base de datos relacional |
| **Keycloak** | 23.0 | Autenticación y autorización |
| **Docker** | Latest | Contenedización |
| **Docker Compose** | Latest | Orquestación de contenedores |
| **Maven** | 3.9+ | Gestor de dependencias |
| **Spring Data JPA** | - | ORM/persistencia |
| **Spring Security** | - | Seguridad y autenticación |
| **OpenFeign** | - | Clientes HTTP para microservicios |
| **Resilience4j** | - | Circuit breaker y reintentos |
| **SpringDoc (Swagger)** | 2.8.4 | Documentación interactiva |

---

## 📂 Estructura del Proyecto

```
TPI-Back/
├── compose.yaml                    # Orquestación Docker Compose
├── pom.xml                         # POM padre (Maven)
├── mvnw / mvnw.cmd                 # Maven wrapper
├── .env                            # Variables de entorno
│
├── servicio_logistico/             # Microservicio principal (8081)
│   ├── pom.xml
│   ├── Dockerfile
│   ├── src/main/java/com/grupo81/
│   │   ├── controller/             # REST Controllers
│   │   ├── services/               # Lógica de negocio
│   │   ├── entity/                 # Entidades JPA
│   │   ├── repository/             # Interfaces JPA
│   │   ├── dtos/                   # Data Transfer Objects
│   │   ├── client/                 # Feign Clients
│   │   └── config/                 # Configuración
│   └── src/main/resources/
│       └── application.yaml        # Configuración
│
├── servicio_cliente/               # Microservicio (8082)
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/...
│
├── servicio_flota/                 # Microservicio (8083)
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/...
│
├── servicio_tarifa/                # Microservicio (8084)
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/...
│
└── _geoapi/geoapi/                 # GeoAPI (8090)
    ├── pom.xml
    ├── Dockerfile
    └── src/...
```

---

## ✅ Requisitos Previos

Antes de ejecutar el sistema, asegúrate de tener instalado:

### Software Necesario
- **Java JDK 21**: [Descargar](https://www.oracle.com/java/technologies/downloads/#java21)
- **Maven 3.9+**: [Descargar](https://maven.apache.org/download.cgi)
- **Docker Desktop**: [Descargar](https://www.docker.com/products/docker-desktop)
- **Git**: [Descargar](https://git-scm.com/download)
- **Postman** (opcional): Para probar APIs

### Verificar Instalación

```powershell
# En PowerShell/CMD
java -version              # Debe mostrar Java 21
mvn --version              # Debe mostrar Maven 3.9+
docker --version           # Debe mostrar Docker
docker-compose --version   # Debe mostrar Docker Compose
```

---

## 🔧 Comandos Esenciales

### 🎯 Construir el Proyecto

```powershell
# Compilar todo el proyecto (todos los módulos)
mvn clean install

# Compilar solo un módulo específico
mvn clean install -pl servicio_logistico

# Compilar sin ejecutar tests
mvn clean install -DskipTests

# Compilar y mostrar logs detallados
mvn clean install -X
```

### 🚀 Ejecutar Microservicios Localmente

```powershell
# Ejecutar un microservicio de forma individual
cd servicio_logistico
mvn spring-boot:run

# En otra terminal
cd servicio_cliente
mvn spring-boot:run

# GeoAPI
cd _geoapi/geoapi
mvn spring-boot:run
```

### 🐳 Ejecutar con Docker Compose

```powershell
# Iniciar todos los servicios (bases de datos, Keycloak, microservicios)
docker-compose up -d

# Ver logs en tiempo real
docker-compose logs -f

# Ver logs de un servicio específico
docker-compose logs -f servicio-logistica

# Detener todos los servicios
docker-compose down

# Detener y eliminar volúmenes (CUIDADO: eliminará datos)
docker-compose down -v

# Reconstruir imágenes
docker-compose up -d --build

# Ver estado de los contenedores
docker-compose ps
```

### 🧪 Ejecutar Tests

```powershell
# Ejecutar todos los tests
mvn test

# Ejecutar tests de un módulo específico
mvn test -pl servicio_logistico

# Ejecutar tests de una clase específica
mvn test -Dtest=NombreDelTestClass

# Ejecutar con cobertura de código
mvn test jacoco:report
```

### 📦 Gestionar Dependencias

```powershell
# Ver árbol de dependencias
mvn dependency:tree

# Ver árbol de dependencias de un módulo
mvn dependency:tree -pl servicio_logistico

# Verificar vulnerabilidades conocidas (CVE)
mvn org.owasp:dependency-check-maven:check

# Actualizar versiones
mvn versions:display-dependency-updates
```

### 🔍 Limpiar y Resets

```powershell
# Limpiar archivos generados
mvn clean

# Limpiar y borrar target
mvn clean -U

# Forzar descarga de dependencias
mvn dependency:resolve --update-snapshots

# Limpiar caché local de Maven
rm -r ~/.m2/repository -Force  # Windows PowerShell
# O en CMD: rmdir %USERPROFILE%\.m2\repository /s /q
```

### 🔗 Ports y URLs Importantes

```
Microservicios:
- Servicio Logística:  http://localhost:8081
- Servicio Cliente:    http://localhost:8082
- Servicio Flota:      http://localhost:8083
- Servicio Tarifa:     http://localhost:8084
- GeoAPI:              http://localhost:8090

Documentación Swagger:
- Logística:   http://localhost:8081/swagger-ui.html
- Cliente:     http://localhost:8082/swagger-ui.html
- Flota:       http://localhost:8083/swagger-ui.html
- Tarifa:      http://localhost:8084/swagger-ui.html
- GeoAPI:      http://localhost:8090/swagger-ui.html

Keycloak:
- Admin Console:  http://localhost:8080/admin
- Usuario: admin
- Contraseña: admin

Bases de Datos PostgreSQL:
- Logística:  localhost:5432/logistica_db
- Cliente:    localhost:5433/cliente_db
- Flota:      localhost:5434/flota_db
- Tarifa:     localhost:5435/tarifa_db
- Keycloak:   localhost:5432/keycloak (en postgres-keycloak)
```

---

## 🚀 Cómo Ejecutar el Sistema

### Opción 1: Ejecución Completa con Docker Compose (RECOMENDADO)

```powershell
# Paso 1: Navega a la carpeta del proyecto
cd TPI-Back

# Paso 2: Construir las imágenes Docker
docker-compose build

# Paso 3: Iniciar todos los servicios
docker-compose up -d

# Paso 4: Verificar que todo está corriendo
docker-compose ps

# Paso 5: Ver logs en tiempo real
docker-compose logs -f

# Acceder a Swagger del servicio logístico
# Abre en el navegador: http://localhost:8081/swagger-ui.html
```

### Opción 2: Ejecución Local sin Docker (Desarrollo)

**Prerequisito:** Tener las bases de datos corriendo primero

```powershell
# Opción A: Solo las bases de datos en Docker
docker-compose up -d postgres-logistica postgres-cliente postgres-flota postgres-tarifa keycloak

# Opción B: En otra terminal, compilar el proyecto
mvn clean install -DskipTests

# Opción C: Ejecutar cada servicio en una terminal separada

# Terminal 1 - Servicio Logística
cd servicio_logistico
mvn spring-boot:run

# Terminal 2 - Servicio Cliente
cd servicio_cliente
mvn spring-boot:run

# Terminal 3 - Servicio Flota
cd servicio_flota
mvn spring-boot:run

# Terminal 4 - Servicio Tarifa
cd servicio_tarifa
mvn spring-boot:run

# Terminal 5 - GeoAPI
cd _geoapi/geoapi
mvn spring-boot:run
```

### Verificar que Todo Funciona

```powershell
# Probar conectividad con cada servicio
curl http://localhost:8081/actuator/health       # Logística
curl http://localhost:8082/actuator/health       # Cliente
curl http://localhost:8083/actuator/health       # Flota
curl http://localhost:8084/actuator/health       # Tarifa
curl http://localhost:8090/actuator/health       # GeoAPI

# Todas deberían responder con: {"status":"UP"}
```

---

## 📱 Microservicios Detallados

### 🚢 Servicio Logístico (8081) - El Corazón del Sistema

#### Responsabilidad
Orquestar todo el proceso de logística de transporte. Es el servicio "maestro" que coordina a los demás.

#### Funciones Principales
1. **Gestionar Solicitudes** - Crear, actualizar, listar solicitudes de transporte
2. **Planificar Rutas** - Calcular rutas con depósitos intermedios
3. **Crear Tramos** - Dividir rutas en segmentos asignables
4. **Asignar Camiones** - Buscar camiones disponibles
5. **Calcular Costos** - Consultar tarifas y calcular totales
6. **Integrar Ubicaciones** - Usar Google Maps para distancias

#### Entidades

**Solicitud**
```
ID: UUID
Número: String (ej: "SOL-001")
Cliente ID: UUID
Origen: Dirección + Lat/Lon
Destino: Dirección + Lat/Lon
Estado: PENDIENTE, EN_TRÁNSITO, COMPLETADA, CANCELADA
Costo Estimado: Decimal
Costo Final: Decimal
Fecha Creación: Timestamp
```

**Ruta**
```
ID: UUID
Solicitud ID: UUID
Cantidad de Tramos: Integer
Cantidad de Depósitos: Integer
Fecha Creación: Timestamp
```

**Tramo**
```
ID: UUID
Ruta ID: UUID
Orden: Integer
Tipo: CARGA, TRANSPORTE, DESCARGA, DEPOSITO
Estado: PENDIENTE, EN_PROGRESO, COMPLETADO
Origen/Destino: Dirección + Coordenadas
Distancia KM: Double
Costo Aproximado/Real: Decimal
Camión ID: UUID
Depósito ID: UUID (opcional)
```

#### Arquitectura Interna

```
Solicitud HTTP
    ↓
SolicitudController
    ↓
SolicitudService (Lógica de negocio)
    ↓
    ├─→ SolicitudRepository (Base de datos)
    ├─→ ClienteServiceClient (Feign → Servicio Cliente)
    ├─→ FlotaServiceClient (Feign → Servicio Flota)
    ├─→ TarifaServiceClient (Feign → Servicio Tarifa)
    └─→ GoogleMapsClient (REST → Google Maps)
    ↓
Respuesta JSON
```

#### Ejemplos de Endpoints

```
POST   /api/solicitudes                    # Crear solicitud
GET    /api/solicitudes                    # Listar todas
GET    /api/solicitudes/{id}               # Obtener por ID
PUT    /api/solicitudes/{id}               # Actualizar
DELETE /api/solicitudes/{id}               # Eliminar

POST   /api/rutas                          # Crear ruta
GET    /api/rutas/solicitud/{solicitudId}  # Rutas de una solicitud

POST   /api/tramos                         # Crear tramo
PUT    /api/tramos/{id}/asignar-camion     # Asignar camión
PUT    /api/tramos/{id}/estado             # Cambiar estado

GET    /api/depositos                      # Listar depósitos
```

---

### 👥 Servicio Cliente (8082)

#### Responsabilidad
Gestionar datos de clientes de la empresa de logística.

#### Entidad Cliente
```
ID: UUID
Nombre: String
Email: String (único)
Teléfono: String
Empresa: String
Dirección: String
Fecha Registro: Timestamp
```

#### Endpoints

```
POST   /api/clientes                    # Crear cliente
GET    /api/clientes                    # Listar todos
GET    /api/clientes/{id}               # Obtener por ID
GET    /api/clientes/email/{email}      # Buscar por email
PUT    /api/clientes/{id}               # Actualizar
DELETE /api/clientes/{id}               # Eliminar
```

---

### 🚚 Servicio Flota (8083)

#### Responsabilidad
Administrar camiones y su disponibilidad.

#### Entidad Camión
```
ID: UUID
Dominio: String (ej: "AAA-100")
Marca/Modelo: String
Capacidad Peso: Double (kg)
Capacidad Volumen: Double (m³)
Transportista: String
Disponible: Boolean
Costo por KM: Decimal
Consumo Combustible: Double (litros/km)
Fecha Registro: Timestamp
```

#### Endpoints

```
POST   /api/camiones                              # Registrar camión
GET    /api/camiones                              # Listar todos
GET    /api/camiones/{id}                         # Obtener por ID
GET    /api/camiones/disponibles                  # Listar disponibles
GET    /api/camiones/por-capacidad                # Buscar por peso/volumen
PUT    /api/camiones/{id}                         # Actualizar
PUT    /api/camiones/{id}/disponibilidad          # Cambiar disponibilidad
DELETE /api/camiones/{id}                         # Eliminar
```

---

### 💰 Servicio Tarifa (8084)

#### Responsabilidad
Gestionar tarifas y calcular costos de transporte.

#### Entidad Tarifa
```
ID: UUID
Código: String (ej: "TAR-BASE-2024")
Descripción: String
Valor: Decimal
Unidad: String (ej: "POR_KM", "FIJA", "POR_KG")
Estado: ACTIVA, INACTIVA
Fecha Creación: Timestamp
```

#### Cálculos Incluidos
- Costo base por KM
- Costo de combustible (distancia × consumo × precio combustible)
- Costo de estadía en depósitos
- Costo de manipulación
- Aplicación de descuentos

#### Endpoints

```
POST   /api/tarifas                      # Crear tarifa
GET    /api/tarifas                      # Listar todas
GET    /api/tarifas/{id}                 # Obtener por ID
GET    /api/tarifas/codigo/{codigo}      # Buscar por código
PUT    /api/tarifas/{id}                 # Actualizar
POST   /api/tarifas/calcular-costo       # Calcular costo de transporte
DELETE /api/tarifas/{id}                 # Eliminar
```

**Ejemplo de Cálculo de Costo:**
```json
{
  "distanciaKm": 150,
  "pesoKg": 5000,
  "tiempoHoras": 3,
  "costoPorKm": 50.00,
  "precioConsumible": 1.50
}
// Resultado: Costo total calculado
```

---

### 🗺️ GeoAPI (8090)

#### Responsabilidad
Centralizar cálculos de distancia usando Google Maps Distance Matrix API.

#### Características
- Integración con Google Maps API
- Cálculo de distancias reales
- Estimación de tiempos
- Respuestas rápidas en caché (opcional)

#### Endpoints

```
GET /api/distancia?origen={origen}&destino={destino}

Ejemplo:
GET /api/distancia?origen=Buenos+Aires,AR&destino=La+Plata,AR

Respuesta:
{
  "origen": "Buenos Aires, Argentina",
  "destino": "La Plata, Argentina",
  "kilometros": 65.5,
  "duracionTexto": "1 hora 15 minutos"
}
```

---

## 📡 API y Endpoints

### Flujo Típico de Creación de Solicitud

```
1. Cliente crea solicitud
   POST /api/solicitudes
   {
     "numeroSolicitud": "SOL-2024-001",
     "clienteId": "uuid-cliente",
     "origenDireccion": "Av. Paseo Colón 500, CABA",
     "destinoDireccion": "Ruta 2 Km 50, La Plata"
   }

2. Sistema crea ruta automáticamente
   POST /api/rutas
   {
     "solicitudId": "uuid-solicitud"
   }

3. Sistema crea tramos
   POST /api/tramos
   {
     "rutaId": "uuid-ruta",
     "tipo": "TRANSPORTE",
     "origenDireccion": "Av. Paseo Colón 500",
     "destinoDireccion": "Ruta 2 Km 50"
   }

4. Sistema busca camión disponible
   GET /api/camiones/disponibles?peso=5000&volumen=10

5. Sistema asigna camión a tramo
   PUT /api/tramos/{tramId}/asignar-camion
   {
     "camionId": "uuid-camion"
   }

6. Sistema calcula costo final
   POST /api/tarifas/calcular-costo
   {
     "distanciaKm": 65.5,
     "pesoKg": 5000
   }

7. Sistema actualiza estado
   PUT /api/solicitudes/{solicitudId}
   {
     "estado": "EN_TRÁNSITO",
     "costoFinal": 5000.00
   }
```

---

## 🔐 Autenticación y Seguridad

### Keycloak - Identity Provider

**¿Qué es?** Sistema de autenticación y autorización OAuth2/OpenID Connect

**Configuración por defecto:**
```
URL: http://localhost:8080
Admin Console: http://localhost:8080/admin
Usuario Admin: admin
Contraseña: admin
Realm: logistica-realm
```

### Roles en el Sistema

```
CLIENTE          - Puede crear solicitudes y ver su estado
OPERADOR         - Puede gestionar rutas y asignaciones
TRANSPORTISTA    - Puede actualizar estado de tramos
ADMIN            - Acceso completo
```

### Flujo de Autenticación

```
1. Usuario accede a http://localhost:8081
2. Sistema redirige a Keycloak
3. Keycloak presenta login
4. Usuario ingresa credenciales
5. Keycloak genera JWT token
6. Frontend almacena token
7. Frontend envía token en cada request:
   Authorization: Bearer <JWT_TOKEN>
8. Spring Security valida el token
9. Si es válido, permite acceso; si no, rechaza (401)
```

### Protección de Endpoints

```java
// Ejemplo en código
@GetMapping("/api/solicitudes")
@PreAuthorize("hasAnyRole('OPERADOR', 'ADMIN')")
public List<SolicitudDTO> listar() {
    // Solo OPERADOR y ADMIN pueden acceder
}
```

### Configuración en application.yaml

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://keycloak:8080/realms/logistica-realm
          jwk-set-uri: http://keycloak:8080/realms/logistica-realm/protocol/openid-connect/certs
```

---

## 🚨 Troubleshooting

### Problema: "Connection refused" en localhost:8081

**Causa:** El servicio no está corriendo

**Solución:**
```powershell
# Verificar que Docker Compose está corriendo
docker-compose ps

# Si no está corriendo, iniciar
docker-compose up -d

# Ver logs de errores
docker-compose logs servicio-logistica
```

### Problema: "PostgreSQL connection timeout"

**Causa:** La base de datos no está lista

**Solución:**
```powershell
# Esperar a que PostgreSQL inicie
docker-compose logs postgres-logistica

# Si sigue fallando, reiniciar
docker-compose down
docker-compose up -d --build
```

### Problema: "Feign Client timeout" entre microservicios

**Causa:** Un servicio no está respondiendo a otro

**Solución:**
```powershell
# Verificar que todos los servicios están corriendo
docker-compose ps

# Ver logs del servicio problematico
docker-compose logs nombre-servicio

# Puede ser un problema de URL, verificar en application.yaml:
# microservices.clientes.url debe ser: http://servicio-cliente:8082
```

### Problema: "Invalid JWT token" en Swagger

**Causa:** El token expiró o es inválido

**Solución:**
1. Cerrar sesión en Swagger UI
2. Hacer click en "Authorize" nuevamente
3. Ingresar credenciales de Keycloak

### Problema: "Permission denied" al ejecutar scripts

**Causa:** Permisos insuficientes

**Solución:**
```powershell
# En PowerShell, ejecutar como administrador
# O permitir scripts:
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

### Problema: Port ya está en uso

**Causa:** Otro proceso está usando el puerto

**Solución:**
```powershell
# Windows - encontrar proceso en puerto 8081
netstat -ano | findstr :8081

# Matar el proceso (reemplazar PID)
taskkill /PID <PID> /F

# O cambiar puerto en docker-compose.yaml
# De: "8081:8081"
# A:  "8082:8081"
```

### Problema: Maven no encuentra dependencias

**Causa:** Caché corrupta

**Solución:**
```powershell
# Limpiar caché local
rm -r ~/.m2/repository -Force

# Limpiar y reinstalar
mvn clean install -U
```

---

## 📚 Recursos Adicionales

### Documentación Oficial
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Cloud Documentation](https://spring.io/projects/spring-cloud)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Docker Documentation](https://docs.docker.com/)
- [Keycloak Documentation](https://www.keycloak.org/documentation)
- [Google Maps API](https://developers.google.com/maps)

### Herramientas Útiles
- **Postman:** Testear APIs
- **DBeaver:** Gestionar PostgreSQL
- **Docker Desktop:** Interfaz gráfica para Docker
- **IntelliJ IDEA:** IDE para Java/Spring

### Lecturas Recomendadas
- "Building Microservices with Spring Boot" - Sam Newman
- "Spring in Action" - Craig Walls
- "Docker in Action" - Jeff Nickoloff

---

## 📞 Contacto y Soporte

**Equipo de Desarrollo:** Grupo 81

**Git Repository:** https://github.com/The-Jaz-band/TPI-Back

**Rama Actual:** `lu`

---

## ✅ Checklist de Verificación

Antes de comenzar, verifica que:

- [ ] Java 21 está instalado (`java -version`)
- [ ] Maven 3.9+ está instalado (`mvn -v`)
- [ ] Docker Desktop está corriendo
- [ ] Puedes hacer `docker ps` sin errores
- [ ] Git está instalado
- [ ] El proyecto está clonado en tu computadora
- [ ] Tienes los puertos 5432-5435, 8080-8084, 8090 disponibles
- [ ] Tienes credenciales de Google Maps API (para GeoAPI)

---

## 🎓 Próximos Pasos

1. **Ejecuta el sistema** siguiendo la sección "Cómo Ejecutar el Sistema"
2. **Explora Swagger UI** en http://localhost:8081/swagger-ui.html
3. **Crea tu primer cliente** usando la API de Cliente
4. **Crea tu primera solicitud** usando la API de Logística
5. **Estudia el código** empezando por `SolicitudController`
6. **Experimenta** con los endpoints y bases de datos

---

**Última actualización:** Noviembre 2025  
**Versión del documento:** 1.0
