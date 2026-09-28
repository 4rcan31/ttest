# Sección 1: Análisis y Diseño

## 1. Arquitectura para un sitio de contenido con 30 millones de visitas al mes

### 1.1 Dimensionamiento

| Dato | Cálculo | Resultado |
|---|---|---|
| Visitas por mes | dato del enunciado | 30,000,000 |
| Visitas por día | 30M / 30 | ≈ 1,000,000 |
| Promedio por segundo | 1M / 86,400 s | ≈ 12 visitas/s |
| Pico estimado (factor ×8 en horas punta o por noticias) | 12 × 8 | ≈ 100 visitas/s |
| Tráfico anónimo (80 %) | 24M visitas/mes | **se puede cachear** en CDN |
| Tráfico autenticado (20 %) | 6M visitas/mes → ≈ 20 visitas/s en pico | contenido personalizado, **no cacheable** en el borde |

Conclusiones del análisis:

1. **El 80 % del tráfico es anónimo y el contenido es el mismo para todos.** Ese tráfico debe
   resolverlo una **CDN** sin llegar a los servidores de origen. Es la decisión que más impacto
   tiene en costo, escalabilidad y tiempo de respuesta.
2. **El 20 % autenticado** necesita respuestas personalizadas. Son unas 20 visitas/s en pico
   (≈ 200–300 llamadas a APIs por segundo), una carga moderada que se cubre con servicios
   *stateless* con autoescalado y una capa de caché (Redis).
3. **Requisito de menos de 4 s:** se apunta a un objetivo más estricto (LCP < 2.5 s en móvil 4G, según
   los *Core Web Vitals*) para tener margen. Se logra con renderizado del lado del servidor o
   estático (SSR/SSG), caché en el borde, optimización de imágenes y JavaScript dividido por ruta.
4. **Móvil y escritorio:** un único frontend *responsive* (mobile first). No hace falta un sitio
   separado para móvil.

### 1.2 Diagrama de arquitectura

```mermaid
flowchart LR
    U["Usuarios<br/>móvil y escritorio"] --> DNS["DNS<br/>Route 53 / Cloudflare"]
    DNS --> CDN["CDN + WAF + protección DDoS<br/>CloudFront / Cloudflare / Front Door"]

    CDN -- "estáticos: JS, CSS, imágenes" --> S3[("Almacenamiento de objetos<br/>S3 / Blob Storage")]
    CDN -- "páginas HTML (SSR/ISR)<br/>anónimas en caché" --> WEB["Frontend Next.js (React)<br/>contenedores con autoescalado"]
    CDN -- "/api/* (sin caché si hay sesión)" --> GW["API Gateway / BFF<br/>rate limiting, validación JWT"]

    WEB --> GW
    GW --> AUTH["Identidad OIDC<br/>Keycloak / Cognito / Auth0"]
    GW --> CS["content-service"]
    GW --> PS["profile-service<br/>(preferencias, favoritos)"]
    GW --> RS["personalization-service<br/>(recomendaciones)"]
    GW --> SS["search-service"]

    CS --> CMS["CMS headless<br/>Strapi / Contentful"]
    CS --> DB1[("PostgreSQL / Aurora<br/>primario + réplicas de lectura")]
    PS --> DB2[("PostgreSQL / Aurora")]
    RS --> NOSQL[("DynamoDB / MongoDB<br/>perfil de comportamiento")]
    SS --> ES[("OpenSearch / Elasticsearch")]
    CS & PS & RS --> REDIS[("Redis<br/>caché y sesiones")]

    CS -. eventos .-> BUS["Kafka / SQS<br/>eventos de contenido y clics"]
    BUS -.-> RS
    BUS -.-> SS

    subgraph Observabilidad
      OBS["OpenTelemetry + Prometheus/Grafana<br/>logs centralizados + RUM (Web Vitals)"]
    end
```

### 1.3 Infraestructura para el hosting del sitio

Recomiendo una **nube pública** (AWS como referencia; Azure y GCP tienen servicios equivalentes)
con **contenedores orquestados** y servicios administrados:

| Capa | Recomendación | Justificación |
|---|---|---|
| DNS y borde | Route 53 + **CloudFront** (o Cloudflare) con **WAF** y AWS Shield | La CDN absorbe el 80 % anónimo. El WAF mitiga el OWASP Top 10 y los bots, y Shield protege contra DDoS. TLS 1.3 y HTTP/2–HTTP/3 terminan en el borde, cerca del usuario. |
| Estáticos y multimedia | **S3** + CDN con optimización de imágenes (WebP/AVIF, tamaños por dispositivo) | Costo bajo, durabilidad alta y entrega rápida a móviles. |
| Cómputo | **Kubernetes administrado (EKS)** o **ECS Fargate**, en **varias zonas de disponibilidad** | Autoescalado horizontal (HPA) según CPU o solicitudes, despliegues *rolling*/*canary* y alta disponibilidad. |
| Base de datos | **Amazon Aurora PostgreSQL/MySQL** Multi-AZ con réplicas de lectura | Conmutación automática ante fallas, respaldos y parches administrados. Las réplicas atienden lecturas. |
| Caché | **ElastiCache Redis** (modo clúster) | Fragmentos personalizados, sesiones, *rate limiting* y resultados de consultas frecuentes. |
| Búsqueda | **OpenSearch** administrado | Búsqueda de texto completo y facetas sobre el contenido. |
| Mensajería | **SQS/SNS** o **MSK (Kafka)** | Desacopla la publicación de contenido de la indexación y de las recomendaciones. |
| Secretos | **Secrets Manager** + **KMS** | Credenciales rotadas y cifrado de datos en reposo. |
| CI/CD e IaC | **GitLab CI** + **Terraform** + Helm/Argo CD | Entornos reproducibles, revisión de cambios y despliegues automatizados. |
| Observabilidad | CloudWatch / **Prometheus + Grafana**, **OpenTelemetry**, ELK/Loki, **RUM** y monitoreo sintético | Permite verificar de forma continua el objetivo de menos de 4 s con usuarios reales (RUM) y generar alertas por SLO. |

**Estrategia de caché por tipo de tráfico** (clave para cumplir el tiempo de respuesta):

| Tráfico | Estrategia |
|---|---|
| Anónimo: páginas de contenido | SSG/ISR: HTML pre-renderizado en la CDN con `Cache-Control: public, s-maxage=300, stale-while-revalidate=600`. Se invalida desde el CMS al publicar. |
| Anónimo: estáticos | Archivos con *hash* en el nombre y `Cache-Control: public, max-age=31536000, immutable`. |
| Autenticado | El HTML base sigue saliendo de la caché. Los bloques personalizados se cargan con llamadas a `/api/*` (`Cache-Control: private, no-store`) que van a Redis o a la base de datos. La CDN no cachea peticiones con cookie de sesión o encabezado `Authorization`. |

**Alta disponibilidad y recuperación ante desastres:** despliegue en 3 zonas de disponibilidad,
mínimo 2 réplicas por servicio, *health checks*, respaldos automáticos con restauración a un punto
en el tiempo (RPO ≤ 5 min), réplica entre regiones del almacenamiento y de la base de datos si el
negocio lo exige (RTO objetivo < 1 h).

### 1.4 Arquitectura de componentes

```mermaid
flowchart TB
    subgraph Presentación
      NEXT["Next.js (React + TypeScript)<br/>SSR/SSG/ISR · responsive · PWA opcional"]
    end
    subgraph "Borde / Entrada"
      BFF["API Gateway / BFF<br/>(Spring Cloud Gateway o Kong)<br/>JWT, CORS, rate limiting, agregación"]
    end
    subgraph "Servicios de dominio (stateless, Spring Boot)"
      C["Contenido<br/>artículos, secciones, multimedia"]
      P["Perfil<br/>preferencias, favoritos, historial"]
      R["Personalización<br/>recomendaciones por intereses"]
      S["Búsqueda"]
      N["Notificaciones<br/>correo / push"]
    end
    subgraph "Plataforma"
      IDP["Proveedor de identidad (OIDC)<br/>login, MFA, redes sociales"]
      MQ["Bus de eventos"]
    end
    NEXT --> BFF --> C & P & R & S
    BFF --> IDP
    C -- ContentPublished --> MQ
    P -- UserActivity --> MQ
    MQ --> R & S & N
```

Principios de diseño:

- **Frontend desacoplado del backend** (arquitectura *headless*): el mismo backend puede servir a
  una app móvil nativa en el futuro.
- **Microservicios por dominio** con base de datos propia (*database per service*), comunicados por
  REST síncrono para consultas y **eventos asíncronos** para propagar cambios. Para un equipo
  pequeño o un MVP, un **monolito modular** con los mismos límites es una alternativa válida que
  luego se puede dividir.
- **Servicios stateless:** la sesión viaja en un **JWT** (OIDC) emitido por el proveedor de
  identidad, lo que permite escalar horizontalmente sin sesiones pegadas a un servidor.
- **BFF (Backend for Frontend):** agrega en una sola llamada lo que la página personalizada
  necesita y reduce los viajes de ida y vuelta desde móviles con alta latencia.
- **Resiliencia:** *timeouts*, reintentos con *backoff*, *circuit breakers* (Resilience4j) y
  degradación controlada. Si falla la personalización, se muestra el contenido genérico.

### 1.5 Bases de datos recomendadas

| Necesidad | Base de datos | Motivo |
|---|---|---|
| Contenido editorial y usuarios (datos relacionales, transacciones) | **PostgreSQL** (o MySQL/MariaDB) administrado: Aurora / RDS | ACID, madurez, réplicas de lectura, índices y JSONB para metadatos flexibles. |
| Caché, sesiones, contadores, *rate limiting* | **Redis** | Latencia de menos de 1 ms; se descarga la base de datos principal. |
| Búsqueda de texto completo y facetas | **OpenSearch / Elasticsearch** | Relevancia, sinónimos, autocompletado y búsqueda que ignora tildes. |
| Perfil de comportamiento para personalización (alto volumen de escritura, esquema flexible) | **DynamoDB** o **MongoDB** | Escala horizontal y lecturas por clave con latencia estable. |
| Multimedia | **S3 / Blob Storage** + CDN | Los archivos binarios no deben guardarse en la base de datos. |
| Analítica | Data warehouse (Redshift / BigQuery) alimentado por eventos | Reportes sin impactar la operación. |

### 1.6 Frameworks recomendados

| Capa | Framework / herramienta | Justificación |
|---|---|---|
| Frontend | **React + Next.js** con TypeScript | SSR/SSG/ISR para SEO y tiempo de carga, *code splitting* automático, optimización de imágenes (`next/image`) y ecosistema amplio. |
| Estilos | Tailwind CSS o CSS Modules | Diseño *responsive* con CSS liviano. |
| Backend | **Java 21 + Spring Boot** (Spring Web, Spring Security OAuth2 Resource Server, Spring Data JPA, Spring Cloud Gateway) | Estándar empresarial, rendimiento sólido con *virtual threads*, seguridad madura y buena observabilidad (Micrometer). Para el BFF también sirve **Node.js (NestJS)**. |
| Identidad | **Keycloak** (autogestionado) o **Cognito / Auth0** | OIDC/OAuth2, MFA, inicio de sesión con redes sociales y políticas de contraseñas sin desarrollarlas desde cero. |
| CMS | **Strapi** / Contentful (headless) | Los editores publican sin despliegues; expone el contenido por API. |
| Migraciones | Flyway / Liquibase | Esquema versionado junto al código. |
| Pruebas | JUnit 5 + Testcontainers, Vitest/Jest + Testing Library, **Playwright** (E2E), **k6/Gatling** (carga) | Se valida la funcionalidad y que el sistema aguante el pico con menos de 4 s. |
| Infraestructura | Docker, Kubernetes (Helm), Terraform, GitLab CI | Automatización y reproducibilidad. |

---

## 2. API de clientes

> Contrato completo en formato OpenAPI 3.1: [`seccion-1-api-clientes.openapi.yaml`](seccion-1-api-clientes.openapi.yaml)

### 2.0 Decisiones de diseño (y una observación de seguridad importante)

- **Recurso:** `/api/v1/customers/{customerCode}`, donde `customerCode` es el código único
  asignado (ejemplo: `CLI-000123`). Se usa el código de negocio y no el id interno, para no
  exponer secuencias.
- **Versionado** en la ruta (`/v1`), JSON como formato de entrada y salida, y errores con el
  estándar **RFC 9457 Problem Details** (`application/problem+json`).
- **Autenticación:** OAuth2 *Bearer token* (JWT) con *scopes* `customers:read` y
  `customers:write`, sobre **TLS 1.2 o superior**.
- **Concurrencia optimista:** cada respuesta incluye un `ETag`. Las actualizaciones envían
  `If-Match` y, si el recurso cambió mientras tanto, la API responde `412 Precondition Failed`.
- **Tarjeta de crédito (PCI DSS):**
  - El **CVV nunca se almacena**, ni cifrado, después de la autorización (PCI DSS v4.0, requisito 3.3.1.2).
    Tampoco se devuelve ni se escribe en los logs. Si la API lo recibe, solo lo reenvía al
    procesador de pagos para validar la tarjeta y lo descarta.
  - El **número de tarjeta (PAN)** no se guarda en claro: se **tokeniza** en la bóveda del
    procesador de pagos (o se cifra con AES-256 y llaves en KMS). Las respuestas solo muestran la
    versión **enmascarada** (`**** **** **** 4242`).
  - **Recomendación:** tokenizar directamente desde el navegador con los *hosted fields* del
    procesador de pagos. Así la API solo recibe un `cardToken`, los datos de la tarjeta nunca pasan
    por nuestros servidores y el alcance PCI se reduce al mínimo (SAQ A).

### 2.1 Obtener información del cliente

```http
GET /api/v1/customers/CLI-000123 HTTP/1.1
Host: api.ejemplo.com
Authorization: Bearer eyJhbGciOiJSUzI1NiIs...
Accept: application/json
```

**Respuesta `200 OK`**

```http
HTTP/1.1 200 OK
Content-Type: application/json
ETag: "7"
Cache-Control: private, no-store
```
```json
{
  "customerCode": "CLI-000123",
  "firstName": "María José",
  "lastName": "Pérez López",
  "address": "Colonia Escalón, Calle La Mascota #123, San Salvador",
  "email": "maria.perez@correo.com",
  "creditCard": {
    "brand": "VISA",
    "maskedNumber": "**** **** **** 4242",
    "last4": "4242"
  },
  "createdAt": "2026-01-10T14:02:11Z",
  "updatedAt": "2026-09-28T15:30:00Z"
}
```

> La respuesta nunca incluye el número completo de la tarjeta ni el CVV.

**Respuesta `404 Not Found`** (formato Problem Details, igual para todos los errores)

```json
{
  "type": "https://api.ejemplo.com/problems/customer-not-found",
  "title": "Cliente no encontrado",
  "status": 404,
  "detail": "No existe un cliente con el código CLI-000999",
  "instance": "/api/v1/customers/CLI-000999"
}
```

### 2.2 Actualizar información del cliente

Se ofrecen dos variantes. **PUT** reemplaza el recurso completo: se envían todos los campos.
**PATCH** hace una actualización parcial: solo se envía lo que cambia, en formato *JSON Merge
Patch*.

**PUT (actualización completa)**

```http
PUT /api/v1/customers/CLI-000123 HTTP/1.1
Host: api.ejemplo.com
Authorization: Bearer eyJhbGciOiJSUzI1NiIs...
Content-Type: application/json
If-Match: "7"
```
```json
{
  "firstName": "María José",
  "lastName": "Pérez López",
  "address": "Residencial Las Flores, Pasaje 3 #12, Santa Tecla",
  "email": "maria.perez@correo.com",
  "creditCard": {
    "number": "4111111111111111",
    "cvv": "123"
  }
}
```

> Alternativa recomendada para `creditCard`: `{ "cardToken": "tok_1Q2w3E4r5T6y" }`, obtenido al
> tokenizar desde el frontend.

**Respuesta `200 OK`**: devuelve el recurso actualizado, con la tarjeta enmascarada y un nuevo `ETag`.

```http
HTTP/1.1 200 OK
Content-Type: application/json
ETag: "8"
```
```json
{
  "customerCode": "CLI-000123",
  "firstName": "María José",
  "lastName": "Pérez López",
  "address": "Residencial Las Flores, Pasaje 3 #12, Santa Tecla",
  "email": "maria.perez@correo.com",
  "creditCard": { "brand": "VISA", "maskedNumber": "**** **** **** 1111", "last4": "1111" },
  "createdAt": "2026-01-10T14:02:11Z",
  "updatedAt": "2026-09-28T16:05:43Z"
}
```

**PATCH (actualización parcial)**

```http
PATCH /api/v1/customers/CLI-000123 HTTP/1.1
Host: api.ejemplo.com
Authorization: Bearer eyJhbGciOiJSUzI1NiIs...
Content-Type: application/merge-patch+json
If-Match: "8"
```
```json
{ "address": "Oficina: Torre Futura, Nivel 10, San Salvador" }
```

Respuesta: `200 OK` con el recurso completo actualizado.

**Respuesta `400 Bad Request`** (validación)

```json
{
  "type": "https://api.ejemplo.com/problems/validation-error",
  "title": "Datos inválidos",
  "status": 400,
  "detail": "La solicitud contiene campos inválidos",
  "instance": "/api/v1/customers/CLI-000123",
  "errors": {
    "email": "El formato del correo electrónico no es válido",
    "creditCard.number": "El número de tarjeta no es válido (Luhn)",
    "creditCard.cvv": "El CVV debe tener 3 o 4 dígitos"
  }
}
```

### 2.3 Eliminar información del cliente

```http
DELETE /api/v1/customers/CLI-000123 HTTP/1.1
Host: api.ejemplo.com
Authorization: Bearer eyJhbGciOiJSUzI1NiIs...
```

**Respuesta `204 No Content`**: sin cuerpo. Además se elimina el token de la tarjeta en la bóveda
del procesador de pagos. Una segunda llamada responde `404 Not Found`, porque el recurso ya no existe.

> Si la regulación exige conservar registros (facturación), se hace una **eliminación lógica** con
> anonimización de los datos personales. Para el cliente, el efecto observable es el mismo: `204`
> y después `404`.

### 2.4 (Complemento) Almacenar un cliente nuevo

```http
POST /api/v1/customers HTTP/1.1
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJSUzI1NiIs...
Idempotency-Key: 4f1c2a9e-5b7d-4f0e-9a51-0c1b2d3e4f5a
```
```json
{
  "customerCode": "CLI-000124",
  "firstName": "Carlos",
  "lastName": "Martínez",
  "address": "Colonia San Benito, Av. Las Magnolias #45",
  "email": "carlos.martinez@correo.com",
  "creditCard": { "cardToken": "tok_9Z8y7X6w5V4u" }
}
```

**Respuesta `201 Created`** con `Location: /api/v1/customers/CLI-000124` y el recurso creado en el
cuerpo. Si el código ya existe, la respuesta es `409 Conflict`.

### 2.5 Resumen de códigos de respuesta HTTP

| Operación | Éxito | Errores posibles |
|---|---|---|
| **GET** `/customers/{code}` | **200 OK** (o **304 Not Modified** si se envía `If-None-Match` con el `ETag` vigente) | 400 código con formato inválido · 401 sin token o token inválido · 403 sin permiso `customers:read` · **404 no existe** · 429 demasiadas solicitudes · 500 / 503 |
| **PUT** `/customers/{code}` | **200 OK** con el recurso actualizado (o 204 si se prefiere no devolver cuerpo) | **400 validación** · 401 · 403 · **404 no existe** · 409 el correo ya pertenece a otro cliente · 412 `If-Match` no coincide (otro proceso lo modificó) · 415 `Content-Type` no soportado · 422 tarjeta rechazada por el procesador · 429 · 500 |
| **PATCH** `/customers/{code}` | **200 OK** | Los mismos que PUT |
| **DELETE** `/customers/{code}` | **204 No Content** | 401 · 403 · **404 no existe** · 409 el cliente tiene operaciones pendientes que impiden eliminarlo · 429 · 500 |
| **POST** `/customers` | **201 Created** + `Location` | 400 · 401 · 403 · **409 el código ya existe** · 415 · 422 · 429 · 500 |

Propiedades HTTP que se respetan: GET es seguro e idempotente; PUT y DELETE son idempotentes
(repetirlos deja el mismo estado); POST no es idempotente, por eso admite `Idempotency-Key` para
reintentos seguros.
