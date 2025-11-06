# Sistema de Logística de Transporte - Proyecto Completo

## 📋 Índice
1. [Estructura del Proyecto](#estructura-del-proyecto)
2. [Microservicios Implementados](#microservicios-implementados)
3. [Modelo de Datos](#modelo-de-datos)
4. [Endpoints Principales](#endpoints-principales)
5. [Configuración de Keycloak](#configuración-de-keycloak)
6. [Instrucciones de Despliegue](#instrucciones-de-despliegue)
7. [Pruebas y Validación](#pruebas-y-validación)

---

## 🏗️ Estructura del Proyecto

```
sistema-logistica/
├── servicio-logistico/          # Microservicio principal (Puerto 8081)
│   ├── src/main/java/com/grupo81/serviciologistico/
│   │   ├── entity/              # Entidades: Deposito, Contenedor, Solicitud, Ruta, Tramo
│   │   ├── repository/          # Repositorios JPA
│   │   ├── service/             # Lógica de negocio
│   │   ├── controller/          # REST Controllers
│   │   ├── dto/                 # DTOs (Request/Response)
│   │   ├── client/              # Feign Clients
│   │   └── config/              # Configuración de seguridad
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/resources/application.yaml
│
├── servicio-cliente/            # Microservicio de clientes (Puerto 8082)
│   ├── src/main/java/com/grupo81/serviciocliente/
│   │   ├── entity/Cliente.java
│   │   ├── repository/ClienteRepository.java
│   │   ├── service/ClienteService.java
│   │   ├── controller/ClienteController.java
│   │   └── dto/
│   ├── pom.xml
│   └── Dockerfile
│
├── servicio-flota/              # Microservicio de flota (Puerto 8083)
│   ├── src/main/java/com/grupo81/servicioflota/
│   │   ├── entity/Camion.java
│   │   ├── repository/CamionRepository.java
│   │   ├── service/CamionService.java
│   │   ├── controller/CamionController.java
│   │   └── dto/
│   ├── pom.xml
│   └── Dockerfile
│
├── servicio-tarifa/             # Microservicio de tarifas (Puerto 8084)
│   ├── src/main/java/com/grupo81/serviciotarifa/
│   │   ├── entity/Tarifa.java
│   │   ├── repository/TarifaRepository.java
│   │   ├── service/TarifaService.java
│   │   ├── controller/TarifaController.java
│   │   └── dto/
│   ├── pom.xml
│   └── Dockerfile
│
└── docker-compose.yml
```

---

## 🔧 Microservicios Implementados

### 1. **Servicio Logística** (Puerto 8081)
**Responsabilidades:**
- Gestión de solicitudes de transporte
- Creación y asignación de rutas
- Gestión de tramos de transporte
- Administración de depósitos
- Seguimiento de contenedores
- Orquestación de otros microservicios

**Entidades Principales:**
- `Solicitud`: Representa una solicitud de transporte
- `Contenedor`: Contenedor a transportar
- `Ruta`: Ruta completa de una solicitud
- `Tramo`: Segmento individual de una ruta
- `Deposito`: Punto de almacenamiento temporal

### 2. **Servicio Cliente** (Puerto 8082)
**Responsabilidades:**
- Registro y gestión de clientes
- Búsqueda de clientes por email
- Actualización de datos de clientes

**Entidad Principal:**
- `Cliente`: Datos del cliente (nombre, email, teléfono, empresa)

### 3. **Servicio Flota** (Puerto 8083)
**Responsabilidades:**
- Registro y gestión de camiones
- Control de disponibilidad de flota
- Búsqueda de camiones por capacidad
- Gestión de transportistas

**Entidad Principal:**
- `Camion`: Datos del camión (dominio, capacidades, costos, consumo)

### 4. **Servicio Tarifa** (Puerto 8084)
**Responsabilidades:**
- Gestión de tarifas y precios
- Cálculo de costos de transporte
- Configuración de valores base
- Cálculo de combustible y estadías

**Entidad Principal:**
- `Tarifa`: Configuración de precios y tarifas

---

## 🗄️ Modelo de Datos

### Relaciones entre Entidades (Servicio Logística)

```
Solicitud (1) ←→ (1) Contenedor
    ↓
Solicitud (1) ←→ (1) Ruta
    ↓
Ruta (1) ←→ (N) Tramo
    ↓
Tramo (N) ←→ (1) Deposito [opcional]
```

### Campos Clave por Entidad

**Solicitud:**
- `id` (UUID), `numero`, `clienteId` (UUID)
- `origenDireccion`, `origenLatitud`, `origenLongitud`
- `destinoDireccion`, `destinoLatitud`, `destinoLongitud`
- `estado`, `costoEstimado`, `costoFinal`

**Contenedor:**
- `id` (UUID), `identificacion`, `pesoKg`, `volumenM3`
- `estadoActual`, `ubicacionActualDireccion`, `clienteId`

**Ruta:**
- `id` (UUID), `solicitudId`, `cantidadTramos`, `cantidadDepositos`

**Tramo:**
- `id` (UUID), `rutaId`, `orden`, `tipo`, `estado`
- `origenDireccion/Lat/Lon`, `destinoDireccion/Lat/Lon`
- `distanciaKm`, `costoAproximado`, `costoReal`
- `fechaHoraInicio`, `fechaHoraFin`, `camionId`, `depositoId`

---

## 🌐 Endpoints Principales

### Servicio Logística (8081)

#### Solicitudes
```
POST   /api/solicitudes                    # Crear solicitud (CLIENTE, OPERADOR)
GET    /api/solicitudes/{id}               # Obtener solicitud
GET    /api/solicitudes/{id}/seguimiento   # Seguimiento (CLIENTE, OPERADOR)
GET    /api/solicitudes/cliente/{clienteId} # Por cliente
GET    /api/solicitudes/pendientes         # Pendientes (OPERADOR)
```

#### Rutas
```
POST   /api/rutas/tentativa?solicitudId=&depositosIds= # Calcular ruta (OPERADOR)
POST   /api/rutas                          # Asignar ruta (OPERADOR)
GET    /api/rutas/solicitud/{solicitudId}  # Obtener ruta
```

#### Tramos
```
PUT    /api/tramos/{id}/asignar-camion    # Asignar camión (OPERADOR)
POST   /api/tramos/{id}/iniciar           # Iniciar tramo (TRANSPORTISTA)
POST   /api/tramos/{id}/finalizar         # Finalizar tramo (TRANSPORTISTA)
GET    /api/tramos/camion/{camionId}      # Tramos por camión
```

#### Depósitos
```
POST   /api/depositos                     # Crear depósito (OPERADOR)
GET    /api/depositos                     # Listar depósitos
GET    /api/depositos/{id}                # Obtener depósito
PUT    /api/depositos/{id}                # Actualizar depósito
GET    /api/depositos/{id}/contenedores   # Contenedores en depósito
```

### Servicio Cliente (8082)
```
POST   /api/clientes                      # Crear cliente
GET    /api/clientes/{id}                 # Obtener cliente
GET    /api/clientes/email/{email}        # Buscar por email
GET    /api/clientes                      # Listar clientes
PUT    /api/clientes/{id}                 # Actualizar cliente
```

### Servicio Flota (8083)
```
POST   /api/camiones                      # Registrar camión (OPERADOR)
GET    /api/camiones/{id}                 # Obtener camión
GET    /api/camiones/disponibles?pesoMinimo=&volumenMinimo= # Disponibles
GET    /api/camiones?disponible=true      # Listar por disponibilidad
PUT    /api/camiones/{id}                 # Actualizar camión
PUT    /api/camiones/{id}/disponibilidad?disponible= # Cambiar disponibilidad
```

### Servicio Tarifa (8084)
```
POST   /api/tarifas                       # Crear tarifa (OPERADOR)
GET    /api/tarifas/configuracion         # Obtener configuración
POST   /api/tarifas/calcular-costo        # Calcular costo
GET    /api/tarifas                       # Listar tarifas
PUT    /api/tarifas/{id}                  # Actualizar tarifa
```

---

## 🔐 Configuración de Keycloak

### 1. Acceder a Keycloak
- URL: http://localhost:8080
- Usuario: `admin`
- Contraseña: `admin`

### 2. Crear Realm
1. Clic en "Master" (arriba izquierda)
2. "Create Realm"
3. Name: `logistica-realm`
4. Save

### 3. Crear Roles
Ir a Realm Settings → Roles → Create Role:
- `CLIENTE`
- `OPERADOR`
- `TRANSPORTISTA`

### 4. Crear Clientes (Clients)
**Para cada microservicio:**

1. Clients → Create Client
   - Client ID: `servicio-logistico` (o el nombre correspondiente)
   - Client Protocol: `openid-connect`
   - Save

2. Configurar el cliente:
   - Access Type: `bearer-only` (para microservicios)
   - Valid Redirect URIs: `*`
   - Save

### 5. Crear Usuarios de Prueba

**Usuario Cliente:**
- Username: `cliente1`
- Email: `cliente1@example.com`
- Role: `CLIENTE`

**Usuario Operador:**
- Username: `operador1`
- Email: `operador1@example.com`
- Role: `OPERADOR`

**Usuario Transportista:**
- Username: `transportista1`
- Email: `transportista1@example.com`
- Role: `TRANSPORTISTA`

### 6. Obtener Token JWT (para pruebas)
```bash
curl -X POST http://localhost:8080/realms/logistica-realm/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=operador1" \
  -d "password=password" \
  -d "grant_type=password" \
  -d "client_id=servicio-logistico"
```

---

## 🚀 Instrucciones de Despliegue

### Prerequisitos
- Docker y Docker Compose instalados
- Java 21 JDK
- Maven 3.9+
- Clave de API de Google Maps (para cálculo de distancias)

### 1. Configurar Variable de Entorno
```bash
export GOOGLE_MAPS_API_KEY="tu-api-key-aqui"
```

### 2. Compilar Microservicios
```bash
# Servicio Logística
cd servicio-logistico
mvn clean package -DskipTests

# Servicio Cliente
cd ../servicio-cliente
mvn clean package -DskipTests

# Servicio Flota
cd ../servicio-flota
mvn clean package -DskipTests

# Servicio Tarifa
cd ../servicio-tarifa
mvn clean package -DskipTests
```

### 3. Iniciar Sistema Completo
```bash
# Desde la raíz del proyecto
docker-compose up -d
```

### 4. Verificar Estado
```bash
docker-compose ps

# Ver logs de un servicio específico
docker-compose logs -f servicio-logistica
```

### 5. Acceder a Servicios
- **Keycloak:** http://localhost:8080
- **Servicio Logística Swagger:** http://localhost:8081/swagger-ui.html
- **Servicio Cliente Swagger:** http://localhost:8082/swagger-ui.html
- **Servicio Flota Swagger:** http://localhost:8083/swagger-ui.html
- **Servicio Tarifa Swagger:** http://localhost:8084/swagger-ui.html

---

## ✅ Pruebas y Validación

### Flujo Completo de Prueba

#### 1. Crear Tarif as Base (OPERADOR)
```json
POST http://localhost:8084/api/tarifas
Authorization: Bearer {token}

{
  "codigoTarifa": "BASE_KM",
  "descripcion": "Costo base por kilómetro",
  "valor": 5.0,
  "unidad": "POR_KM"
}
```

Repetir para:
- `COMBUSTIBLE_LITRO` (1.5)
- `CONSUMO_PROMEDIO_L_KM` (0.35)
- `GESTION_TRAMO` (100.0)
- `ESTADIA_DIARIO` (50.0)

#### 2. Crear Depósitos (OPERADOR)
```json
POST http://localhost:8081/api/depositos

{
  "nombre": "Depósito Central",
  "direccion": "Av. Principal 123",
  "latitud": -31.4201,
  "longitud": -64.1888,
  "costoEstadiaDiario": 50.0
}
```

#### 3. Registrar Camiones (OPERADOR)
```json
POST http://localhost:8083/api/camiones

{
  "dominio": "ABC123",
  "nombreTransportista": "Juan Pérez",
  "telefonoTransportista": "+54912345678",
  "capacidadPesoKg": 5000,
  "capacidadVolumenM3": 30,
  "costoBaseKm": 8.0,
  "consumoCombustibleLKm": 0.4
}
```

#### 4. Crear Solicitud de Transporte (CLIENTE)
```json
POST http://localhost:8081/api/solicitudes

{
  "contenedor": {
    "identificacion": "CONT-001",
    "pesoKg": 3000,
    "volumenM3": 20
  },
  "cliente": {
    "nombre": "Empresa Constructora S.A.",
    "email": "contacto@constructora.com",
    "telefono": "+54911234567",
    "empresa": "Constructora S.A."
  },
  "origen": {
    "direccion": "Calle Origen 100",
    "latitud": -31.4135,
    "longitud": -64.1811
  },
  "destino": {
    "direccion": "Calle Destino 200",
    "latitud": -31.4400,
    "longitud": -64.2000
  }
}
```

#### 5. Calcular Ruta Tentativa (OPERADOR)
```
GET http://localhost:8081/api/rutas/tentativa?solicitudId={id}&depositosIds={deposito1Id},{deposito2Id}
```

#### 6. Asignar Ruta (OPERADOR)
```json
POST http://localhost:8081/api/rutas

{
  "solicitudId": "{solicitudId}",
  "depositosIds": ["{deposito1Id}"]
}
```

#### 7. Asignar Camión a Tramo (OPERADOR)
```json
PUT http://localhost:8081/api/tramos/{tramoId}/asignar-camion

{
  "camionId": "{camionId}"
}
```

#### 8. Iniciar Tramo (TRANSPORTISTA)
```
POST http://localhost:8081/api/tramos/{tramoId}/iniciar
```

#### 9. Finalizar Tramo (TRANSPORTISTA)
```
POST http://localhost:8081/api/tramos/{tramoId}/finalizar
```

#### 10. Consultar Seguimiento (CLIENTE)
```
GET http://localhost:8081/api/solicitudes/{solicitudId}/seguimiento
```

---

## 📊 Estados del Sistema

### Estados de Solicitud
- `BORRADOR`: Solicitud creada, sin ruta asignada
- `PROGRAMADA`: Ruta asignada, esperando inicio
- `EN_TRANSITO`: Al menos un tramo iniciado
- `ENTREGADA`: Todos los tramos finalizados
- `CANCELADA`: Solicitud cancelada

### Estados de Contenedor
- `EN_ORIGEN`: En ubicación de origen
- `RETIRADO`: Primer tramo iniciado
- `EN_VIAJE`: En tránsito
- `EN_DEPOSITO`: En punto intermedio
- `ENTREGADO`: En destino final

### Estados de Tramo
- `ESTIMADO`: Creado pero sin camión
- `ASIGNADO`: Camión asignado
- `INICIADO`: En curso
- `FINALIZADO`: Completado
- `CANCELADO`: Cancelado

---

## 🛠️ Herramientas de Desarrollo

### Swagger/OpenAPI
Cada microservicio expone su documentación:
- http://localhost:8081/swagger-ui.html
- http://localhost:8082/swagger-ui.html
- http://localhost:8083/swagger-ui.html
- http://localhost:8084/swagger-ui.html

### Actuator Endpoints
- `/actuator/health` - Estado del servicio
- `/actuator/info` - Información del servicio

### Logs
Ubicación: `logs/servicio-logistica.log` (en cada microservicio)

---

## 📝 Notas Importantes

1. **UUID como identificadores**: Todas las entidades usan UUID para mayor seguridad y escalabilidad

2. **Validaciones de negocio**:
   - Camión debe tener capacidad suficiente
   - Tramos deben completarse en orden
   - No se puede iniciar tramo sin camión asignado

3. **Cálculo de costos**:
   - Aproximado: Usa valores promedio
   - Real: Usa datos específicos del camión asignado
   - Incluye: traslado + combustible + estadía + gestión

4. **Integración Google Maps**:
   - Calcula distancias reales entre puntos
   - Requiere API Key válida
   - Fallback a cálculo euclidiano si falla

5. **Seguridad**:
   - Todos los endpoints requieren autenticación JWT
   - Roles específicos por operación
   - Tokens propagados entre microservicios (Feign)

---

## 🎯 Cumplimiento de Requerimientos

✅ Registrar solicitud de transporte (Cliente)
✅ Consultar estado del transporte (Cliente)
✅ Consultar rutas tentativas con costos (Operador)
✅ Asignar ruta a solicitud (Operador)
✅ Consultar contenedores pendientes (Operador)
✅ Asignar camión a tramo (Operador)
✅ Iniciar/Finalizar tramo (Transportista)
✅ Calcular costo total incluyendo estadías
✅ Registrar depósitos, camiones y tarifas
✅ Validar capacidades de camiones
✅ Integración con API externa (Google Maps)
✅ Seguridad con Keycloak y JWT
✅ Documentación con Swagger
✅ Microservicios independientes
✅ Despliegue con Docker Compose

---

Este proyecto está listo para ser desplegado y evaluado según los criterios del TPI de Backend de Aplicaciones 2025.
