# Guía completa del Sistema de Logística de Transporte

> **Objetivo de este documento**
>
> Explicar TODO el código del proyecto paso a paso para personas que están aprendiendo Java y Spring Boot. No necesitas haber trabajado antes con microservicios: iremos armando el rompecabezas desde cero.
>
> ¿Cómo leer esta guía? te propongo seguir las secciones en orden:
>
> 1. Panorama general: qué problema resuelve el sistema y cómo está organizado.
> 2. Herramientas y dependencias: qué tecnologías usa y para qué sirven.
> 3. Microservicios uno por uno (incluyendo el nuevo **GeoAPI**).
> 4. Comunicación entre servicios.
> 5. Seguridad con Keycloak y JWT.
> 6. Base de datos y entidades.
> 7. Docker y despliegue paso a paso.
> 8. Recorrido guiado por el código (método por método, con ejemplos).
> 9. Flujo de negocio completo.
> 10. Tips para seguir aprendiendo.
>
> Si en algún momento te perdés, volvé a esta lista y retomá desde el punto anterior.

---

## 1. Panorama general

### 1.1 Problema que resuelve
El proyecto modela un sistema de logística de transporte para contenedores marítimos. Las empresas necesitan:

- **Registrar solicitudes** de traslado de contenedores.
- **Planificar rutas** (posibles trayectos y depósitos intermedios).
- **Asignar camiones** disponibles para cubrir los tramos.
- **Calcular costos** aproximados y reales del transporte.
- **Seguir el estado** de los contenedores.

Para cubrir todas estas necesidades el sistema se dividió en **microservicios independientes** que conversan entre sí.

### 1.2 Arquitectura por microservicios
En lugar de un único proyecto grande, tenemos varias aplicaciones Spring Boot bajo la carpeta `TPI-Back/`:

| Módulo / Microservicio | Puerto | Responsable principal |
|------------------------|--------|-----------------------|
| `servicio_logistico`   | 8081   | Orquestar todo el proceso de transporte. Maneja solicitudes, rutas, tramos y depósitos. También integra tarifas, flota y distancias.
| `servicio_cliente`     | 8082   | Administrar clientes (datos de contacto, empresa, etc.).
| `servicio_flota`       | 8083   | Administrar camiones disponibles y su capacidad.
| `servicio_tarifa`      | 8084   | Guardar parámetros de tarifa y calcular costos.
| `_geoapi/geoapi`       | 8090 (configurable) | Microservicio auxiliar que concentra llamadas a Google Maps Distance Matrix (Geo API). Devuelve distancias y tiempos entre ubicaciones.

> **Nota:** El microservicio GeoAPI se encuentra en la carpeta `_geoapi/geoapi/` y puede ejecutarse de forma independiente o dentro del ecosistema Docker. Su objetivo es encapsular la lógica de cálculo de distancias para que el resto del sistema pueda reutilizarla fácilmente.

### 1.3 Organización Maven
`pom.xml` en la raíz es el **POM padre (packaging POM)**. Indica qué módulos (microservicios) existen y define versiones comunes de dependencias.

- Cada microservicio (`servicio_*`) tiene su propio `pom.xml` que hereda del padre (`<parent>`). Allí agrega dependencias específicas.
- El proyecto GeoAPI también es un proyecto Maven independiente con su propio `pom.xml` (
`_geoapi/geoapi/pom.xml`). Comparte la misma versión de Spring Boot y Java (21).

Esto simplifica la configuración y garantiza que todos usen la misma versión de Spring Boot / Spring Cloud.

---

## 2. Herramientas y dependencias principales

### 2.1 Java y Spring Boot
- **Java 21**: versión utilizada. El proyecto requiere que tengas instalado JDK 21.
- **Spring Boot 3.5.7**: framework que acelera el desarrollo backend. Viene con componentes integrados para web, seguridad, JPA, etc.

### 2.2 Dependencias comunes (definidas en el POM padre)
- `spring-boot-starter-web`: crea API REST.
- `spring-boot-starter-data-jpa`: simplifica acceso a bases de datos con entidades y repositorios.
- `spring-boot-starter-validation`: valida datos de entrada con anotaciones como `@NotNull`, `@Email`.
- `spring-boot-starter-security` + `spring-boot-starter-oauth2-resource-server`: protegen endpoints con tokens JWT de Keycloak.
- `springdoc-openapi-starter-webmvc-ui`: genera documentación interactiva (Swagger UI).
- `spring-boot-starter-actuator`: expone endpoints con información de salud del servicio.
- `spring-cloud-starter-openfeign`: permite declarar clientes HTTP para llamar a otros microservicios como si fueran interfaces Java.
- `spring-cloud-dependencies`: conjunto de librerías para arquitecturas distribuidas.
- `resilience4j-*`: circuit breaker y reintentos configurados en el servicio logístico.
- `lombok`: genera automáticamente getters, setters, constructores, etc.
- `org.postgresql:postgresql`: driver JDBC para PostgreSQL.

### 2.3 Dependencias específicas
- **GeoAPI** usa `RestClient` (nuevo HTTP client de Spring) para hablar con la API de Google.
- **Google Maps Distance Matrix**: se usa para obtener distancias reales. Antes se llamaba directo desde `servicio_logistico`. Con GeoAPI ahora se puede delegar esa lógica a un servicio dedicado.
- **Docker Compose**: no es una dependencia de Maven, pero sí parte central del entorno. Orquesta todas las bases de datos, Keycloak, microservicios y GeoAPI con un solo comando.

### 2.4 Configuración externa (YAML)
Cada microservicio tiene un `application.yaml` donde configuramos:

- Puerto del servidor.
- Datos de conexión a la base de datos.
- Perfil activo (`prod` por defecto, `dev` para desarrollo sin seguridad estricta).
- URLs de otros microservicios (para Feign).
- Parámetros de seguridad (issuer-uri y jwk-set-uri de Keycloak).
- Ajustes de logging, Swagger, Actuator, etc.

GeoAPI también tiene `application.yml` con su puerto y la API Key de Google (`google.maps.apikey`). Idealmente la API Key se inyecta por variable de entorno y **no** se versiona, pero para fines educativos aparece un valor de ejemplo.

---

## 3. Microservicios en detalle

### 3.1 Estructura común
Todos los microservicios siguen el mismo patrón de capas:

```
src/main/java/com/grupo81
 ├── controller/    -> API REST (recibe y responde HTTP)
 ├── services/      -> Lógica de negocio
 ├── repository/    -> Acceso a base de datos (interfaces JPA)
 ├── entity/        -> Modelos persistentes (tablas)
 ├── dtos/          -> Objetos para transferir datos (request/response)
 ├── config/        -> Configuración especial (seguridad, Swagger, etc.)
 └── ...
```

GeoAPI replica la estructura, aunque más simple (`controller`, `service`, `model`).

### 3.2 Servicio Logística (`servicio_logistico`)
Es el microservicio más grande. Funciones principales:

- Crear solicitudes (`Solicitud`) asociadas a un contenedor y a un cliente.
- Calcular rutas tentativas y definitivas usando depósitos intermedios.
- Crear tramos de viaje (`Tramo`) y asignar camiones.
- Coordinar con los otros microservicios mediante Feign Clients:
  - `ClienteServiceClient`: busca o crea clientes en `servicio_cliente`.
  - `FlotaServiceClient`: consulta camiones disponibles en `servicio_flota`.
  - `TarifaServiceClient`: obtiene costos base y calcula costos finales en `servicio_tarifa`.
- Integrarse con **GeoAPI** o Google Maps directamente para calcular distancias reales.
- Aplicar seguridad por roles (CLIENTE, OPERADOR, TRANSPORTISTA).

#### Clases clave
- `ServicioLogisticoApplication`: clase principal con `main`. Anotada con `@SpringBootApplication` y `@EnableFeignClients`.
- `SolicitudController`, `RutaController`, `TramoController`, `DepositoController`: exposición de endpoints REST.
- `SolicitudService`, `RutaService`, `TramoService`, `DepositoService`: lógica de negocio.
- `entity` package: `Solicitud`, `Contenedor`, `Ruta`, `Tramo`, `Deposito`.
- `repository` package: interfaces JPA como `SolicitudRepository`.
- `dtos`: múltiples subpaquetes para requests y responses.
- `config`:
  - `SecurityConfig` (prod) y `SecurityConfigDev` (dev) para la seguridad.
  - `OpenApiConfig` para personalizar documentación Swagger.

#### Integración con GeoAPI
- Puedes agregar un `FeignClient` que apunte a GeoAPI (`http://geoapi:8090/api/distancia`) cuando corras el ecosistema Docker.
- Alternativamente, `RutaService` ya incluye un cliente `GoogleMapsClient`. GeoAPI brinda un punto centralizado con lógica similar para que otros equipos o proyectos puedan consumir distancias sin repetir código.

### 3.3 Servicio Cliente (`servicio_cliente`)
- Administra CRUD de clientes.
- `ClienteController`: endpoints `/api/clientes` para crear, buscar por ID/email, listar y actualizar.
- `ClienteService`: valida duplicados (email) y transforma entidades en DTOs.
- Entidad `Cliente` con timestamps y campos básicos.

### 3.4 Servicio Flota (`servicio_flota`)
- Maneja disponibilidad de camiones.
- `CamionController`: registra camiones, lista disponibles según peso/volumen, actualiza info y disponibilidad.
- `CamionService`: lógica de validaciones y armado de DTOs.
- `Camion`: entidad con datos del transportista, capacidades y costos por km.

### 3.5 Servicio Tarifa (`servicio_tarifa`)
- Administra parámetros de costos y hace cálculos.
- `TarifaController`: crear tarifa, obtener configuración, calcular costo, listar y actualizar.
- `TarifaService`: reglas de negocio (garantiza códigos únicos, calcula costos combinando distancia, combustible, estadías, gestión).
- `Tarifa`: entidad persistente con código, descripción, valor y unidad.

### 3.6 GeoAPI (`_geoapi/geoapi`)
- Objetivo: encapsular la lógica de distancia para proyectos educativos.
- `GeoapiApplication`: clase principal.
- `GeoController` (`GET /api/distancia`): recibe parámetros `origen` y `destino` en formato de texto (por ejemplo, `Buenos Aires,AR`).
- `GeoService`:
  - Usa `RestClient.Builder` para llamar a `https://maps.googleapis.com/maps/api/distancematrix/json`.
  - Inyecta la API Key desde `application.yml` (`google.maps.apikey`).
  - Parsea la respuesta con `ObjectMapper` y mapea a `DistanciaDTO` (kilómetros y duración textual).
- `DistanciaDTO`: simple clase con `origen`, `destino`, `kilometros`, `duracionTexto`.
- Swagger UI disponible en `http://localhost:8090/swagger-ui.html` (o el puerto que definas).

> **Tip:** Podés dockerizar GeoAPI con un Dockerfile similar al resto o ejecutarlo con `mvn spring-boot:run`. En Compose podrías agregar un servicio extra que exponga el puerto 8090.

---

## 4. Comunicación entre microservicios

### 4.1 Feign Clients
En `servicio_logistico` hay interfaces anotadas con `@FeignClient`. Ejemplo `ClienteServiceClient`:

```java
@FeignClient(
    name = "servicio-cliente",
    url = "${microservices.clientes.url}",
    configuration = FeignClientConfiguration.class
)
public interface ClienteServiceClient {
    @PostMapping("/api/clientes")
    ClienteDTO crearCliente(@RequestBody ClienteCreateDTO cliente);

    @GetMapping("/api/clientes/{id}")
    ClienteDTO obtenerCliente(@PathVariable("id") UUID id);

    @GetMapping("/api/clientes/email/{email}")
    ClienteDTO buscarPorEmail(@PathVariable("email") String email);
}
```

Cuando `SolicitudService` necesita crear o buscar un cliente, simplemente invoca `clienteServiceClient.buscarPorEmail(email)`. Spring genera el código HTTP detrás de escena.

### 4.2 Seguridad entre llamadas
`FeignClientConfiguration` agrega un interceptor para copiar el header `Authorization` del request original y reenviarlo al microservicio destino. Así el token JWT se comparte y todos validan al mismo usuario.

### 4.3 Circuit Breaker y Retry
En `application.yaml` del servicio logístico se configura Resilience4j. Esto evita que una caída temporal de otro microservicio derribe todo el sistema:

```yaml
resilience4j:
  circuitbreaker:
    instances:
      clientesService:
        slidingWindowSize: 10
        failureRateThreshold: 50
        waitDurationInOpenState: 5s
  retry:
    instances:
      clientesService:
        maxAttempts: 3
        waitDuration: 1s
```

Puedes replicar configuraciones similares para GeoAPI si lo exponés como un servicio aparte.

---

## 5. Seguridad con Keycloak y JWT

### 5.1 Idea básica
- Keycloak actúa como **servidor de identidad**.
- Cada microservicio es un recurso protegido que requiere tokens JWT emitidos por Keycloak.
- Roles (`CLIENTE`, `OPERADOR`, `TRANSPORTISTA`) definen qué endpoints se pueden usar.

### 5.2 Configuración en el código
- Cada `application.yaml` apunta al realm `logistica-realm`.
- `SecurityConfig` define reglas: por ejemplo, `POST /api/solicitudes` lo pueden hacer roles CLIENTE u OPERADOR, `POST /api/tramos/{id}/iniciar` solo TRANSPORTISTA.
- `SecurityConfigDev` (perfil dev) deshabilita seguridad para facilitar pruebas.

### 5.3 Documentos de apoyo
En la raíz hay dos guías:
- `guia_keycloak_completa.md`: explica cómo levantar Keycloak, crear realm, roles y usuarios.
- `guia_docker_completa.md`: muestra cómo desplegar con Docker.

---

## 6. Base de datos y entidades

Cada microservicio tiene su propia base (URL, usuario y password distintos). Usa PostgreSQL y mapea tablas vía JPA.

| Servicio            | Tabla principal | Resumen de campos |
|--------------------|-----------------|-------------------|
| `servicio_logistico` | `solicitudes`, `contenedores`, `rutas`, `tramos`, `depositos` | Guarda toda la logística.
| `servicio_cliente`   | `clientes` | Datos básicos del cliente.
| `servicio_flota`     | `camiones` | Información del camión y su disponibilidad.
| `servicio_tarifa`    | `tarifas`  | Parámetros de costos.

Las entidades usan `UUID` como identificador generado automáticamente (`@GeneratedValue(strategy = GenerationType.UUID)`). Esto evita colisiones y es más seguro que números incrementales.

### 6.1 Relaciones clave en logística
- `Solicitud` tiene un `Contenedor` (uno a uno).
- `Solicitud` se vincula con `Ruta` (uno a uno): la ruta describe el plan completo.
- `Ruta` tiene muchos `Tramo` (uno a muchos).
- `Tramo` puede apuntar a un `Deposito` y llevar un `camionId` (referencia al servicio flota).

---

## 7. Docker y despliegue

### 7.1 Docker Compose central (`compose.yaml`)
El archivo `compose.yaml` arma el entorno completo:

- **Bases de datos**: 5 contenedores Postgres (uno por microservicio + uno para Keycloak).
- **Keycloak**: configuración con usuario admin/admin.
- **Microservicios**: cada uno con su Dockerfile propio.
- **Variables de entorno**:
  - Conexiones a DB (`SPRING_DATASOURCE_URL`, `USERNAME`, `PASSWORD`).
  - URLs internas para Feign (`MICROSERVICES_*_URL`).
  - Configuración de Swagger OAuth.
  - API Key de Google Maps (`MICROSERVICES_GOOGLE_MAPS_API_KEY`).
- **Red**: todos los servicios comparten `logistica-network`.
- **Volúmenes**: persiste datos de Postgres entre reinicios.

Para levantar todo (excepto GeoAPI, que puedes agregar manualmente):

```bash
docker compose up -d --build
```

> **Importante:** Define `GOOGLE_MAPS_API_KEY` en tu entorno (`.env` o variables del sistema). Compose lo inyecta al servicio logístico.

### 7.2 Añadir GeoAPI al Compose (opcional)
Puedes extender `compose.yaml` con un servicio adicional:

```yaml
  geoapi:
    build:
      context: ./_geoapi/geoapi
      dockerfile: Dockerfile # si creas uno, similar al resto
    environment:
      GOOGLE_MAPS_APIKEY: ${GOOGLE_MAPS_API_KEY}
    ports:
      - "8090:8080"
    networks:
      - logistica-network
```

Luego, ajusta `servicio_logistico` para que apunte a `http://geoapi:8080` como origen de distancias.

### 7.3 Ejecución manual (sin Docker)
1. **Compilar** cada microservicio:
   ```bash
   mvn -pl servicio_logistico clean package -DskipTests
   mvn -pl servicio_cliente clean package -DskipTests
   mvn -pl servicio_flota clean package -DskipTests
   mvn -pl servicio_tarifa clean package -DskipTests
   mvn -f _geoapi/geoapi/pom.xml clean package -DskipTests
   ```
2. **Levantar dependencias** (Postgres, Keycloak) con Docker o local.
3. **Ejecutar** cada servicio con `mvn spring-boot:run`. Usa perfil `dev` si quieres desactivar seguridad:
   ```bash
   mvn -pl servicio_logistico spring-boot:run -Dspring-boot.run.profiles=dev
   ```
4. **GeoAPI** se arranca igual:
   ```bash
   mvn -f _geoapi/geoapi/pom.xml spring-boot:run
   ```

---

## 8. Recorrido guiado por el código

### 8.1 Servicio Logística

#### 8.1.1 Clase principal
`ServicioLogisticoApplication` @servicio_logistico/src/main/java/com/grupo81/ServicioLogisticoApplication.java#6-13

#### 8.1.2 Controladores (resumen)
- `SolicitudController` @servicio_logistico/src/main/java/com/grupo81/controller/SolicitudController.java#21-82
- `RutaController` @servicio_logistico/src/main/java/com/grupo81/controller/RutaController.java#20-61
- `TramoController` @servicio_logistico/src/main/java/com/grupo81/controller/TramoController.java
- `DepositoController` @servicio_logistico/src/main/java/com/grupo81/controller/DepositoController.java#21-79

#### 8.1.3 Servicios destacados
- `SolicitudService` @servicio_logistico/src/main/java/com/grupo81/services/SolicitudService.java#33-195
- `RutaService` @servicio_logistico/src/main/java/com/grupo81/services/RutaService.java#31-327 (observa métodos `calcularRutaTentativa` y `asignarRuta`).
- `TramoService`, `DepositoService` (estructura similar).

#### 8.1.4 Repositorios y entidades
- `SolicitudRepository` @servicio_logistico/src/main/java/com/grupo81/repository/SolicitudRepository.java#13-28
- `Solicitud` @servicio_logistico/src/main/java/com/grupo81/entity/Solicitud.java#15-89
- `Tramo`, `Ruta`, `Deposito`, `Contenedor`: en la misma carpeta `entity`.

### 8.2 Servicio Cliente
- `ClienteController` @servicio_cliente/src/main/java/com/grupo81/controller/ClienteController.java#22-74
- `ClienteService` @servicio_cliente/src/main/java/com/grupo81/services/ClienteService.java#21-96
- `ClienteRepository` y `Cliente` @servicio_cliente/src/main/java/com/grupo81/repository/ClienteRepository.java & @servicio_cliente/src/main/java/com/grupo81/entity/Cliente.java

### 8.3 Servicio Flota
- `CamionController` @servicio_flota/src/main/java/com/grupo81/controller/CamionController.java#20-87
- `CamionService` @servicio_flota/src/main/java/com/grupo81/services/CamionService.java#19-127
- `Camion` @servicio_flota/src/main/java/com/grupo81/entity/Camion.java#11-54

### 8.4 Servicio Tarifa
- `TarifaController` @servicio_tarifa/src/main/java/com/grupo81/controller/TarifaController.java#19-72
- `TarifaService` @servicio_tarifa/src/main/java/com/grupo81/services/TarifaService.java#21-165
- `Tarifa` @servicio_tarifa/src/main/java/com/grupo81/entity/Tarifa.java#11-51

### 8.5 GeoAPI
- `GeoController` @_geoapi/geoapi/src/main/java/utnfc/isi/back/spring/geoapi/controller/GeoController.java#7-17
- `GeoService` @_geoapi/geoapi/src/main/java/utnfc/isi/back/spring/geoapi/service/GeoService.java#12-42
- `DistanciaDTO` @_geoapi/geoapi/src/main/java/utnfc/isi/back/spring/geoapi/model/DistanciaDTO.java#5-11

---

## 9. Flujo completo de negocio (ejemplo práctico)

1. **Cliente registra solicitud** (`POST /api/solicitudes`):
   - Envía datos del contenedor, origen, destino, datos del cliente.
   - Servicio logístico verifica email del cliente (servicio cliente) y crea el contenedor.
   - Guarda la solicitud en estado BORRADOR.

2. **Operador calcula ruta tentativa** (`POST /api/rutas/tentativa`):
   - Pasa `solicitudId` + lista de depósitos.
   - Se calculan tramos con distancias (GeoAPI o Google Maps) y costos (servicio tarifa).
   - Devuelve resumen con costo estimado y tiempo.

3. **Operador asigna ruta** (`POST /api/rutas`):
   - Usa `RutaAsignacionRequestDTO` con IDs de depósitos.
   - Se crea ruta definitiva, tramos y se actualiza la solicitud a PROGRAMADA.

4. **Operador asigna camiones** (`PUT /api/tramos/{id}/asignar-camion`):
   - Consulta camiones disponibles en servicio flota.
   - Asigna uno y cambia estado del tramo a ASIGNADO.

5. **Transportista inicia tramo** (`POST /api/tramos/{id}/iniciar`):
   - Cambia estado a INICIADO.
   - Contenedor pasa a EN_VIAJE.

6. **Transportista finaliza tramo** (`POST /api/tramos/{id}/finalizar`):
   - Cambia estado a FINALIZADO y registra costos reales.
   - Si es el último tramo, solicitud pasa a ENTREGADA.

7. **Cliente consulta seguimiento** (`GET /api/solicitudes/{id}/seguimiento`):
   - Obtiene estado actual, ubicación del contenedor y costos estimados.

---

## 10. Tips para seguir aprendiendo

1. **Configurar Postman o Thunder Client** para probar endpoints con tokens JWT de Keycloak.
2. **Usar Swagger** (`/swagger-ui.html`) para entender los contratos REST.
3. **Activar más logs** (`logging.level.com.grupo81=DEBUG`) para ver queries SQL y flujos.
4. **Extender tests**: agrega tests unitarios/integación para `SolicitudService`, `RutaService`, `GeoService`.
5. **Implementar pendientes**: por ejemplo, `GET /api/rutas/solicitud/{solicitudId}` en `RutaController`.
6. **Integrar GeoAPI** al servicio logístico vía Feign para desacoplar la lógica de distancias.
7. **Crear un Dockerfile para GeoAPI** y agregarlo al Compose.

---

## 11. Glosario rápido

- **Microservicio**: aplicación independiente con responsabilidad específica.
- **DTO (Data Transfer Object)**: objeto que viaja en la API (no es la entidad de DB).
- **Entidad**: clase mapeada a una tabla de DB.
- **Repository**: interfaz que accede a la DB con métodos CRUD.
- **Servicio**: capa con reglas de negocio.
- **Feign Client**: interfaz que representa un cliente HTTP para otros servicios.
- **JWT**: token de autenticación firmado.
- **Keycloak**: servidor de gestión de identidades y accesos.
- **Swagger / Springdoc**: documentación automática de APIs.
- **Actuator**: endpoints `/actuator/health` para monitorear aplicaciones Spring Boot.
- **GeoAPI**: microservicio auxiliar que encapsula consultas a Google Maps.

---

## 12. Conclusión

Con esta guía:

- Viste la arquitectura completa, incluyendo el nuevo GeoAPI.
- Entendiste cómo se distribuye la responsabilidad entre microservicios.
- Analizaste dependencias, configuración y orquestación con Docker Compose.
- Recorriste controladores, servicios, repositorios y entidades clave.
- Aprendiste cómo se comunican los microservicios y cómo se asegura el sistema con Keycloak.
- Exploraste un flujo de negocio de punta a punta.

¡Éxitos con el aprendizaje! Cualquier duda, vuelve a esta guía o revisa los archivos fuente indicados. 💪
