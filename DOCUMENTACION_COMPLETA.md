# Guía completa del Sistema de Logística de Transporte

> **Objetivo de este documento**
>
> Explicar TODO el código del proyecto paso a paso para personas que están aprendiendo Java y Spring Boot. No necesitas haber trabajado antes con microservicios: iremos armando el rompecabezas desde cero.
>
> ¿Cómo leer esta guía? te propongo seguir las secciones en orden:
>
> 1. Panorama general: qué problema resuelve el sistema y cómo está organizado.
> 2. Herramientas y dependencias: qué tecnologías usa y para qué sirven.
> 3. Microservicios uno por uno: qué hace cada uno y cómo está armado su código.
> 4. Comunicación entre servicios: cómo se hablan y comparten datos.
> 5. Seguridad con Keycloak y JWT.
> 6. Base de datos y entidades.
> 7. Ejecutar el proyecto paso a paso.
> 8. Recorrido guiado por el código: método por método, con ejemplos.
>
> Si en algún momento te perdés, volvé a esta lista y retomá desde el punto anterior. 

---

## 1. Panorama general

### 1.1 Problema que resuelve
El proyecto modela un sistema de logística de transporte para contenedores marítimos. Las empresas necesitan saber:

- **Registrar solicitudes** de traslado de contenedores.
- **Planificar rutas** (posibles trayectos y depósitos intermedios).
- **Asignar camiones** disponibles para cubrir los tramos.
- **Calcular costos** aproximados y reales del transporte.
- **Seguir el estado** de los contenedores.

Para cubrir todas estas necesidades el sistema se dividió en **cuatro microservicios** independientes que conversan entre sí.

### 1.2 Arquitectura por microservicios
En lugar de un único proyecto grande, tenemos 4 aplicaciones Spring Boot separadas bajo la misma carpeta raíz `TPI-Back/`:

| Microservicio       | Puerto | Responsable principal |
|--------------------|--------|-----------------------|
| `servicio_logistico` | 8081   | Orquestar todo el proceso de transporte. Maneja solicitudes, rutas, tramos y depósitos.
| `servicio_cliente`   | 8082   | Administrar clientes (datos de contacto, empresa, etc.).
| `servicio_flota`     | 8083   | Administrar camiones disponibles y su capacidad.
| `servicio_tarifa`    | 8084   | Guardar parámetros de tarifa y calcular costos.

Cada microservicio corre su propio servidor web (Spring Boot) y expone endpoints REST basados en JSON.

### 1.3 Organización Maven
`pom.xml` en la raíz es el **POM padre**. Indica qué módulos (microservicios) existen y define versiones comunes de dependencias.

- `servicio_logistico/pom.xml` hereda del padre y agrega dependencias propias.
- Lo mismo ocurre con los otros tres módulos.

Esto simplifica la configuración y garantiza que todos usen la misma versión de Spring Boot / Spring Cloud.

---

## 2. Herramientas y dependencias principales

### 2.1 Java y Spring Boot
- **Java 21**: versión utilizada. El proyecto pide que tengas instalado JDK 21.
- **Spring Boot 3.5.7**: framework que acelera el desarrollo backend. Viene con componentes integrados para web, seguridad, JPA, etc.

### 2.2 Dependencias comunes
En el `pom.xml` padre encontrarás:

- `spring-boot-starter-web`: crea API REST.
- `spring-boot-starter-data-jpa`: simplifica acceso a bases de datos con entidades y repositorios.
- `spring-boot-starter-validation`: valida datos de entrada con anotaciones como `@NotNull`, `@Email`.
- `spring-boot-starter-security` + `spring-boot-starter-oauth2-resource-server`: protegen endpoints con tokens JWT de Keycloak.
- `springdoc-openapi-starter-webmvc-ui`: genera documentación interactiva (Swagger UI).
- `spring-boot-starter-actuator`: expone endpoints con información de salud del servicio.
- `spring-cloud-starter-openfeign`: permite declarar clientes HTTP para llamar a otros microservicios como si fueran interfaces Java.
- `spring-cloud-dependencies`: conjunto de librerías para trabajar con arquitecturas distribuidas.

### 2.3 Otras librerías destacadas
- **Lombok**: genera automáticamente getters, setters, constructores, etc. Se habilita con anotaciones como `@Data`, `@Builder`.
- **Feign Client**: consumidores HTTP declarativos. Tenemos interfaces como `ClienteServiceClient` que describen los endpoints del servicio cliente.
- **Resilience4j** (en `servicio_logistico`): circuit breaker + retires para que, si otro microservicio falla temporariamente, el sistema degrade su funcionalidad de forma controlada.
- **Jakarta Validation**: validaciones (`@NotBlank`, `@Size`, etc.).
- **PostgreSQL driver**: permite conectarse a bases Postgres.

### 2.4 Configuración externa (YAML)
Cada microservicio tiene un `application.yaml` donde configuramos:

- Puerto del servidor.
- Datos de conexión a la base de datos.
- Perfil activo (`prod` por defecto, `dev` para desarrollo sin seguridad estricta).
- URLs de otros microservicios (para Feign).
- Parámetros de seguridad (issuer-uri y jwk-set-uri de Keycloak).
- Ajustes de logging, Swagger, Actuator, etc.

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

### 3.2 Servicio Logística (`servicio_logistico`)
Es el microservicio más grande. Funciones principales:

- Crear solicitudes (`Solicitud`) asociadas a un contenedor y a un cliente.
- Calcular rutas tentativas y definitivas usando depósitos intermedios.
- Crear tramos de viaje (`Tramo`) y asignar camiones.
- Coordinar con los otros microservicios mediante Feign Clients:
  - `ClienteServiceClient`: busca o crea clientes.
  - `FlotaServiceClient`: consulta camiones disponibles.
  - `TarifaServiceClient`: obtiene costos base y calcula costos finales.
- Integrarse con **Google Maps Directions API** para calcular distancias reales.
- Aplicar seguridad por roles (CLIENTE, OPERADOR, TRANSPORTISTA).

#### Clases clave
- `ServicioLogisticoApplication`: clase principal con `main`. Anotada con `@SpringBootApplication` y `@EnableFeignClients`.
- `SolicitudController`: expone endpoints para crear solicitudes, ver estado y listarlas.
- `RutaController`: permite calcular rutas tentativas, asignar rutas definitivas a una solicitud.
- `TramoController`: gestionar tramos (asignar camión, iniciar, finalizar).
- `DepositoController`: CRUD de depósitos y contenedores almacenados.
- `SolicitudService`, `RutaService`, `TramoService`, `DepositoService`: lógica de negocio.
- `entity` package: `Solicitud`, `Contenedor`, `Ruta`, `Tramo`, `Deposito`.
- `repository` package: interfaces JPA que consultan tablas (ej.: `SolicitudRepository`).
- `dtos`: múltiples subpaquetes para requests y responses.
- `config`:
  - `SecurityConfig`: configuración de seguridad para producción (requiere JWT).
  - `SecurityConfigDev`: variante sin seguridad estricta cuando el perfil `dev` está activo.
  - `OpenApiConfig`: personaliza la documentación Swagger.

### 3.3 Servicio Cliente (`servicio_cliente`)
Se encarga de CRUD de clientes:

- `ClienteController`: endpoints `/api/clientes` para crear, buscar por ID/email, listar y actualizar.
- `ClienteService`: lógica de negocio; valida que no haya emails duplicados y usa `ClienteRepository`.
- `Cliente`: entidad con campos `nombre`, `email`, `telefono`, `empresa` y timestamps.

### 3.4 Servicio Flota (`servicio_flota`)
Maneja la disponibilidad de camiones:

- `CamionController`: registra camiones, lista disponibles según peso/volumen, actualiza info y disponibilidad.
- `CamionService`: crea camiones, busca por ID, filtra disponibles. Usa `CamionRepository` para guardar `Camion`.
- `Camion`: entidad con datos de capacidad y costos por km.

### 3.5 Servicio Tarifa (`servicio_tarifa`)
Administra parámetros de costos y hace cálculos:

- `TarifaController`: endpoints para crear tarifa, obtener configuración, calcular costo, listar y actualizar.
- `TarifaService`: implementa reglas:
  - Garantiza códigos únicos (`BASE_KM`, `COMBUSTIBLE_LITRO`, etc.).
  - Arma una estructura `ConfiguracionTarifaDTO` con valores por defecto si aún no existen.
  - Calcula costos combinando distancia, consumo combustible, estadías, gestión de tramos.
- `Tarifa`: entidad persistente con código, descripción, valor y unidad.

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
`FeignClientConfiguration` agrega un interceptor para copiar el header `Authorization` del request original y reenviarlo al microservicio destino. De esta forma, el token JWT se comparte y todos los servicios validan al mismo usuario.

### 4.3 Circuit Breaker y Retry
En `application.yaml` del servicio logístico se configura Resilience4j para los clientes Feign. Esto evita que una caída temporal de otro microservicio derribe todo el sistema:

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

---

## 5. Seguridad con Keycloak y JWT

### 5.1 Idea básica
- Keycloak actúa como **servidor de identidad**.
- Cada microservicio es un recurso protegido que requiere tokens **Bearer** (JWT) emitidos por Keycloak.
- Los roles (`CLIENTE`, `OPERADOR`, `TRANSPORTISTA`) definen qué endpoints se pueden usar.

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

Cada microservicio tiene su propia base (URL, usuario y password diferentes). Usa PostgreSQL y mapea tablas vía JPA.

| Servicio            | Tabla principal | Resumen de campos |
|--------------------|-----------------|-------------------|
| `servicio_logistico` | `solicitudes`, `contenedores`, `rutas`, `tramos`, `depositos` | Guarda toda la logística.
| `servicio_cliente`   | `clientes` | Datos básicos del cliente.
| `servicio_flota`     | `camiones` | Información del camión y su disponibilidad.
| `servicio_tarifa`    | `tarifas`  | Parámetros de costos.

Las entidades usan `UUID` como identificador generado automáticamente (`@GeneratedValue(strategy = GenerationType.UUID)`). Esto evita colisiones y es más seguro que números incrementales.

### 6.1 Relación de entidades en logística
- `Solicitud` tiene un `Contenedor` (uno a uno).
- `Solicitud` se vincula con `Ruta` (uno a uno): la ruta describe el plan completo.
- `Ruta` tiene muchos `Tramo` (uno a muchos).
- `Tramo` puede apuntar a un `Deposito` y llevar un `camionId` (referencia al servicio flota).

---

## 7. Cómo ejecutar el proyecto (
### 7.1 Requisitos previos
- Java 21
- Maven 3.9+
- Docker Desktop (para levantar bases de datos y Keycloak con `compose.yaml`)
- API Key de Google Maps (puedes usar una falsa para pruebas básicas)

### 7.2 Pasos rápidos
1. **Clonar** el repo y entrar en `TPI-Back/`.
2. **Configurar Keycloak** siguiendo `guia_keycloak_completa.md` (crear realm, roles y usuarios).
3. **Construir** cada microservicio:
   ```bash
   mvn -pl servicio_logistico clean package -DskipTests
   mvn -pl servicio_cliente clean package -DskipTests
   mvn -pl servicio_flota clean package -DskipTests
   mvn -pl servicio_tarifa clean package -DskipTests
   ```
4. **Levantar infraestructura** con Docker Compose:
   ```bash
   docker compose up -d
   ```
   Esto levanta Keycloak, bases de datos y (opcionalmente) los microservicios.
5. **Configurar variable** `GOOGLE_MAPS_API_KEY` si quieres distancias reales.
6. **Acceder a Swagger** de cada servicio: `http://localhost:8081/swagger-ui.html`, etc.

### 7.3 Perfil de desarrollo
Si no quieres configurar seguridad mientras aprendes, usa el perfil `dev`. Por ejemplo, en `servicio_logistico` ejecuta:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev -pl servicio_logistico
```

Esto activará `SecurityConfigDev`, permitiendo llamar a los endpoints sin token.

---

## 8. Recorrido guiado por el código

Vamos servicio por servicio revisando clases importantes.

### 8.1 Servicio Logística

#### 8.1.1 Clase principal
`ServicioLogisticoApplication` (ruta `servicio_logistico/src/main/java/com/grupo81/ServicioLogisticoApplication.java`):

```java
@SpringBootApplication
@EnableFeignClients
public class ServicioLogisticoApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServicioLogisticoApplication.class, args);
    }
}
```

- `@SpringBootApplication`: habilita auto-configuración de Spring.
- `@EnableFeignClients`: activa Feign para declarar clientes HTTP.

#### 8.1.2 Controladores
Cada controlador expone endpoints REST. Resumimos sus métodos principales:

1. **`SolicitudController`**
   - `POST /api/solicitudes`: recibe un `SolicitudCreateRequestDTO`, valida datos y crea una solicitud nueva (`SolicitudService.crearSolicitud`).
   - `GET /api/solicitudes/{id}`: recupera una solicitud por UUID.
   - `GET /api/solicitudes/{id}/seguimiento`: devuelve estado y ubicación de un contenedor.
   - `GET /api/solicitudes/cliente/{clienteId}`: lista solicitudes del cliente.
   - `GET /api/solicitudes/pendientes`: muestra solicitudes con estado BORRADOR/PROGRAMADA/EN_TRANSITO.

2. **`RutaController`**
   - `POST /api/rutas/tentativa`: calcula ruta tentativa pasando `solicitudId` y lista de depósitos. Usa `RutaService.calcularRutaTentativa`.
   - `POST /api/rutas`: asigna ruta definitiva con `RutaAsignacionRequestDTO`. Cambia el estado de la solicitud a PROGRAMADA.
   - `GET /api/rutas/solicitud/{solicitudId}`: pendiente de implementación (comentado como TODO).

3. **`TramoController`**
   - `PUT /api/tramos/{id}/asignar-camion`: asigna camión disponible a un tramo.
   - `POST /api/tramos/{id}/iniciar`: marca tramo como iniciado (solo rol TRANSPORTISTA).
   - `POST /api/tramos/{id}/finalizar`: marca tramo como finalizado.
   - `GET /api/tramos/camion/{camionId}`: obtiene tramos de un camión específico.

4. **`DepositoController`**
   - CRUD completo de depósitos.
   - `GET /api/depositos/{id}/contenedores`: lista contenedores almacenados en ese depósito.

#### 8.1.3 Servicios (lógica)
Los servicios son componentes `@Service` que encapsulan reglas de negocio.

**`SolicitudService`**:

- `crearSolicitud`:
  1. Verifica que el contenedor no exista (`contendedorRepository.existsByIdentificacion`).
  2. Busca cliente por email usando `clienteServiceClient`. Si no existe, lo crea.
  3. Crea entidad `Contenedor` y la guarda.
  4. Crea entidad `Solicitud` con estado inicial BORRADOR y la guarda.
  5. Devuelve un `SolicitudResponseDTO` (mapeo a DTO.
- `obtenerSolicitud`: busca en el repositorio y lanza error si no existe.
- `obtenerSeguimiento`: arma respuesta con información actual de contenedor y costos estimados.
- `listarSolicitudesPorCliente` y `listarSolicitudesPendientes` usan métodos del repositorio.

**`RutaService`**:

- `calcularRutaTentativa`: la parte más extensa. Pasos:
  1. Trae solicitud y depósitos.
  2. Llama a `tarifaServiceClient.obtenerConfiguracion()` para tener valores de costo base, combustible, etc.
  3. Va creando tramos tentativos:
     - Origen a primer depósito (si existe).
     - Entre depósitos intermedios.
     - Último tramo al destino final.
  4. Para cada tramo calcula distancia con Google Maps (`GoogleMapsClient.obtenerDirecciones`). Si falla, usa valor fallback.
  5. Calcula costo en base a distancia y configuración de tarifas.
  6. Suma costos de gestión por tramo.
  7. Devuelve `RutaTentativaResponseDTO` con lista de tramos, totales de costo, tiempo y distancia.

- `asignarRuta`:
  1. Verifica que la solicitud exista y esté en estado BORRADOR.
  2. Recalcula ruta tentativa (para asegurarse de la información actualizada).
  3. Crea entidad `Ruta` y guarda.
  4. Convierte cada `TramoTentativoDTO` en entidad `Tramo` asociada.
  5. Actualiza solicitud a estado PROGRAMADA con costos y tiempos estimados.

**`TramoService` y `DepositoService`** (no se muestran completos, pero siguen el mismo patrón):
- Validan datos.
- Interactúan con repositorios para guardar/actualizar entidades.
- Mapean resultados a DTOs.

#### 8.1.4 Repositorios
Interfaces que extienden `JpaRepository` y definen queries personalizadas si se necesitan.

Ejemplo `SolicitudRepository`:
```java
@Repository
public interface SolicitudRepository extends JpaRepository<Solicitud, UUID> {
    Optional<Solicitud> findByNumero(String numero);

    @Query("SELECT s FROM Solicitud s WHERE s.clienteId = :clienteId ORDER BY s.fechaCreacion DESC")
    List<Solicitud> findByClienteId(@Param("clienteId") UUID clienteId);

    @Query("SELECT s FROM Solicitud s WHERE s.estado IN :estados ORDER BY s.fechaCreacion DESC")
    List<Solicitud> findByEstadoIn(@Param("estados") List<Solicitud.EstadoSolicitud> estados);

    @Query("SELECT COUNT(s) FROM Solicitud s WHERE FUNCTION('DATE', s.fechaCreacion) = CURRENT_DATE")
    Long countSolicitudesToday();
}
```

#### 8.1.5 Entidades
Cada entidad mapea a una tabla con anotaciones JPA (`@Entity`, `@Table`). Usan `@Builder`, `@Data` de Lombok.

- **`Solicitud`**: tiene campos de origen y destino, costos, estado (`BORRADOR`, `PROGRAMADA`, etc.), timestamps.
- **`Contenedor`**: guarda identificación, peso, volumen, estado actual y ubicación.
- **`Ruta`**: referencia a `Solicitud` y contiene una colección de `Tramo`.
- **`Tramo`**: representa segmento (origen/destino, tipo, costos, estado, fechas).
- **`Deposito`**: almacén con dirección, coordenadas y costos de estadía.

#### 8.1.6 DTOs
Se utilizan para separar la entidad (que representa la base de datos) del formato que viaja por la red.

- Requests: `SolicitudCreateRequestDTO`, `RutaAsignacionRequestDTO`, etc.
- Responses: `SolicitudResponseDTO`, `RutaTentativaResponseDTO`, `TramoResponseDTO`.
- DTOs anidados: por ejemplo, `SolicitudCreateRequestDTO` incluye `ClienteRequestDTO`, `UbicacionDTO` y datos del contenedor.

### 8.2 Servicio Cliente

#### 8.2.1 Controlador
`ClienteController` define endpoints protegidos por roles. Ejemplo `crearCliente`:

```java
@PostMapping
@PreAuthorize("hasAnyRole('OPERADOR', 'CLIENTE')")
public ResponseEntity<ClienteResponseDTO> crearCliente(@Valid @RequestBody ClienteCreateRequestDTO request) {
    ClienteResponseDTO response = clienteService.crearCliente(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}
```

- `@PreAuthorize`: verifica que el usuario tenga rol permitido.
- `@Valid`: activa validaciones configuradas en el DTO.
- Llama a `ClienteService` para la lógica.

#### 8.2.2 Servicio
`ClienteService` implementa métodos como `crearCliente`, `obtenerCliente`, `buscarPorEmail`, `listarClientes`, `actualizarCliente`.

- `crearCliente`: valida que el email no exista (`clienteRepository.existsByEmail`). Si todo es válido, crea entidad `Cliente` y la guarda.
- `mapToDTO`: transforma entidad en `ClienteResponseDTO`.

#### 8.2.3 Entidad y repositorio
`Cliente` define columnas con restricciones (`length`, `nullable`, `unique`). `ClienteRepository` extiende `JpaRepository` y agrega métodos como `existsByEmail` y `findByEmail`.

### 8.3 Servicio Flota

- `CamionController`: expone endpoints para registrar, actualizar, y obtener camiones.
- `CamionService`:
  - `crearCamion`: valida dominio único, construye `Camion`, guarda y devuelve DTO.
  - `obtenerCamion`: busca por ID.
  - `obtenerCamionesDisponibles`: llama a repositorio con parámetros `pesoMinimo`, `volumenMinimo`.
  - `actualizarDisponibilidad`: cambia campo `disponible`.
- `CamionRepository` (no se mostró, pero existe) define queries personalizadas como `findCamionesDisponibles`.
- `Camion` entidad: almacena datos del transportista, capacidades y costos.

### 8.4 Servicio Tarifa

- `TarifaController`: REST para crear, listar y actualizar tarifas, calcular costo y obtener configuración.
- `TarifaService`:
  - `crearTarifa`: asegura códigos únicos y guarda la entidad.
  - `obtenerConfiguracion`: arma `ConfiguracionTarifaDTO` con valores guardados o defaults.
  - `calcularCosto`: realiza cálculos combinando distancia, consumo, gestión y estadía.
  - `listarTarifas`, `actualizarTarifa` sencillos.
- `Tarifa` entidad: tiene enum `UnidadMedida` con valores `POR_KM`, `POR_LITRO`, etc.

---

## 9. Flujo completo de negocio (ejemplo práctico)

Imagina que un cliente quiere transportar un contenedor.

1. **Cliente registra solicitud** (`POST /api/solicitudes` en servicio logístico):
   - Se envían datos del contenedor, origen, destino, datos del cliente.
   - El servicio logístico verifica si el email ya existe. Si no, llama al servicio cliente para crearlo.
   - Se almacena la solicitud y el contenedor en estado BORRADOR y EN_ORIGEN.

2. **Operador calcula ruta tentativa** (`POST /api/rutas/tentativa`):
   - Pasa `solicitudId` + lista de depósitos.
   - Se calculan tramos con distancias (Google Maps) y costos (tarifa).
   - Devuelve resumen con costo estimado y tiempo.

3. **Operador asigna ruta** (`POST /api/rutas`):
   - Usa `RutaAsignacionRequestDTO` con IDs de depósitos.
   - Se crea ruta definitiva, tramos y se actualiza estado de la solicitud a PROGRAMADA.

4. **Operador asigna camiones** a cada tramo (`PUT /api/tramos/{id}/asignar-camion`):
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

## 10. Cómo leer y aprender del código

### 10.1 Recomendaciones prácticas
1. **Configurar un IDE** (IntelliJ / VS Code) con soporte para Lombok.
2. **Revisar DTOs** antes de los controladores para entender qué datos se envían y reciben.
3. **Seguir el flujo** desde controller -> service -> repository -> entity.
4. **Usar Swagger** para probar endpoints dinámicamente.
5. **Agregar logs** o usar Actuator cuando quieras ver qué está pasando.
6. **Ejecutar testeos** (cuando agregues nuevos) para validar reglas de negocio.

### 10.2 Hooks de aprendizaje
- Cuando veas `@Transactional`, significa que todas las operaciones de DB dentro de ese método se ejecutarán en una transacción (si algo falla se hace rollback).
- `@Builder` permite crear instancias con código limpio:
  ```java
  Cliente cliente = Cliente.builder()
      .nombre("Ana")
      .email("ana@example.com")
      .telefono("123456")
      .empresa("Constructora")
      .build();
  ```
- Feign: piensa que cada método anotado es un request HTTP (GET, POST, etc.).
- Validaciones: revisa los DTOs en `dtos/.../request` para ver reglas (`@NotBlank`, `@Size`).

---

## 11. Recursos adicionales

- **`README.md`**: resumen ejecutivo del proyecto.
- **`guia_docker_completa.md`**: instrucciones detalladas para levantar el entorno completo con Docker.
- **`guia_keycloak_completa.md`**: paso a paso para configurar Keycloak (realm, clientes, usuarios).
- **Swagger UI**: documentación generada automáticamente de cada microservicio.
- **Actuator**: endpoints `/actuator/health` para verificar estado.

---

## 12. Próximos pasos sugeridos para aprender

1. **Correr el proyecto en perfil dev** y probar endpoints sin seguridad.
2. **Agregar Postman** o cURL para interactuar manualmente.
3. **Implementar el TODO** en `RutaController` (`GET /api/rutas/solicitud/{solicitudId}`) como ejercicio.
4. **Agregar tests** unitarios para `SolicitudService` y `RutaService`.
5. **Implementar front-end** simple para consumir APIs.

---

## 13. Glosario rápido

- **Microservicio**: aplicación independiente con responsabilidad específica.
- **DTO (Data Transfer Object)**: objeto que viaja en la API (no es la entidad de DB).
- **Entidad**: clase mapeada a una tabla de DB.
- **Repository**: interfaz que accede a la DB con métodos CRUD.
- **Servicio**: capa con reglas de negocio.
- **Feign Client**: interfaz que representa un cliente HTTP para otros servicios.
- **JWT**: token de autenticación firmado.
- **Keycloak**: servidor de gestión de identidades y accesos.
- **Swagger / Springdoc**: documentación automática de APIs.
- **Actuator**: endpoints para monitorear aplicaciones Spring Boot.

---

## 14. Conclusión

Llegaste al final de la guía 🎉. Recapitulando:

- Viste la arquitectura general y qué resuelve el sistema.
- Entendiste cómo se distribuye la responsabilidad entre microservicios.
- Analizaste dependencias y configuración.
- Recorriste controladores, servicios, repositorios y entidades clave.
- Aprendiste cómo se comunican los microservicios con Feign y cómo se asegura el sistema con Keycloak.
- Practicaste un recorrido end-to-end de una solicitud real.

Con esto deberías sentirte más cómodo navegando el código, entendiendo cómo está armado y preparado para hacer mejoras o agregar nuevas funcionalidades.

¡Éxitos con el aprendizaje y cualquier duda vuelve a esta guía! 💪
