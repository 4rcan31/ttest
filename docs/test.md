# EVALUACIÓN TÉCNICA

Nombre: ________________________________  
Fecha: ____________

---

# Sección 1: Análisis y Diseño

## 1.

Describa la arquitectura recomendada para implementar una aplicación web de contenido con **30 millones de visitas al mes**, en la cual el **80% del tráfico es anónimo** y el **20% restante es de usuarios que al hacer login consultan información personalizada**.

Tome en consideración que el sitio web puede ser accedido a través de dispositivos móviles y computadoras, y se requiere un tiempo de respuesta menor a **4 segundos** en la carga de la aplicación.

Su respuesta debe considerar lo siguiente:

- Infraestructura para hosting del sitio
- Arquitectura de componentes
- Bases de datos recomendadas
- Frameworks


R:

Utilizaría infraestructura en nube con alta disponibilidad, distribuyendo los servicios en varias zonas de disponibilidad. El frontend estático y el contenido público se servirían mediante almacenamiento de objetos y una CDN, aprovechando que el 80 % del tráfico es anónimo y puede ser cacheado. El backend se ejecutaría en contenedores administrados mediante Kubernetes, permitiendo escalado horizontal según demanda. Redis se utilizaría como capa de caché para reducir consultas repetitivas y mejorar los tiempos de respuesta.

Utilizaría una arquitectura desacoplada: React para el frontend, un reverse proxy/API Gateway como punto de entrada y servicios backend stateless. Dependiendo del tamaño del sistema, estos servicios pueden implementarse inicialmente como un monolito modular y posteriormente separarse en microservicios por dominio. Para procesos que no necesitan respuesta inmediata utilizaría mensajería asíncrona mediante Kafka o RabbitMQ.


Usuario
   ↓
CDN
   ↓
React
   ↓
API Gateway / Nginx
   ↓
Backend
 ├─ Auth
 ├─ Content
 └─ Profile
   ↓
PostgreSQL / Redis

Utilizaría PostgreSQL como base principal por su soporte transaccional, estabilidad y capacidad de escalamiento. Los servicios pueden mantener separación lógica de sus datos mediante bases o esquemas independientes. Redis serviría como caché y, si aparecen necesidades específicas como búsqueda de texto, grandes volúmenes de eventos o grafos, se podrían incorporar motores especializados.

Frontend: React con TypeScript y diseño responsive.
Backend: Node.js con NestJS, Spring Boot Java, .NET c#, Flask/Django python.
Base de datos: PostgreSQL.
Caché: Redis.
Infraestructura: Docker y Kubernetes.
Gateway/reverse proxy: Nginx o Kong.
Mensajería asíncrona, si es necesaria: Kafka o RabbitMQ.

## 2.

Basado en su respuesta anterior, tome en consideración que se desea añadir un set de APIs para almacenar y actualizar información de clientes, tomando como identificador un código único asignado.

Dentro de la información que se puede actualizar se encuentra:

a. Nombre  
b. Apellido  
c. Dirección  
d. Dirección de correo  
e. Número de tarjeta de crédito  
f. CVV de la tarjeta de crédito  

### a)

Se solicita que haga el diseño del API con la que usted implementaría las operaciones de:

- Obtener información del cliente
- Actualizar información del cliente
- Eliminar dicha información

Escriba la sintaxis de las peticiones a la API REST tomando en consideración que la respuesta debe expresarse en formato JSON.

Indique también el código de respuesta HTTP del resultado de cada petición.



R:

- Objeto: El crud se maneja al rededor de un objeto, en este caso posiblemente una persona cn los campos que se requiere:

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



- **Recurso:** `/api/v1/customers/{customerCode}`, donde `customerCode` es el código único
  asignado (ejemplo: `CLI-000123`). Se usa el código de negocio y no el id interno, para no
  exponer secuencias.
- **Versionado** en la ruta (`/v1`), JSON como formato de entrada y salida, y errores con el
  estándar **RFC 9457 Problem Details** (`application/problem+json`).
  - **Autenticación:** OAuth2 *Bearer token* (JWT) con *scopes* `customers:read` y
  `customers:write`, sobre **TLS 1.2 o superior**. se debe de diseñar el alcance por scopes.. si son pocos pueden ir en el jwt si no es mejor mantenerlos en la base
  - **Concurrencia optimista:** cada respuesta incluye un `ETag`. Las actualizaciones envían
  `If-Match` y, si el recurso cambió mientras tanto, la API responde `412 Precondition Failed`. Esto para evitar actualizaciones silenciosas de versiones del objetivo consultado desfasado

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


Patch puede no ser idepontente dependiendo de la operacion que haga

Patch idepontente
{
  "name": "Pedro"
}


patch no idepotente

{
  "incrementBalance": 10
}
No arroja el msimo resultado al ejecutarlo mas de una vez, incrementa un valor

, y put se usa en casos especiales donde se actualiza el 100% del objeto, se sobreescribe ; 

POST tiene la particualidad que debe de evitar la duplicidad de la misma intencion logica, alguien que da varios clicks por ejemplo, por eso el idempotency key
---

# Sección 2: Caso Práctico

Se solicita desarrollar una aplicación para el manejo de un carrito de compras de artículos deportivos de tipo responsive.

El aplicativo debe tener opción para:

## Registro de usuarios nuevos

### Observaciones

El registro debe solicitar:

- Nombres
- Apellidos
- Dirección de envío
- Email, validando formato
- Fecha de nacimiento, mayores de 18 años
- Password

Todos los datos son obligatorios.

## Login

Login de usuarios existentes para gestión de sus compras.

## Perfil

Opción para consultar el perfil del cliente y poder actualizar información.

## Recuperación de password

Opción de recuperación de password.

## Búsqueda de artículos

Opción para búsqueda de artículos del inventario para la venta.

## Catálogo de artículos

El catálogo de artículos debe manejar:

- Imagen del artículo
- Descripción
- Monto
- Cantidad disponible

El catálogo de artículos puede ser fijo, es decir, no se necesita un mantenimiento para cargar artículos.

## Carretilla de compras

Se debe manejar una carretilla de compras por usuario, en la cual se pueda ir viendo el resumen de los artículos seleccionados.

Los artículos deben poder eliminarse del carrito si así se quisiera.

La carretilla deberá presentar una opción para poder confirmar el pedido, en donde se muestre la dirección de envío del cliente y una opción para editar esa dirección si se desea cambiar.

Una vez se confirme el pedido, presentar en pantalla:

- El resultado de la confirmación
- El número de orden generado

## Órdenes generadas

El aplicativo debe tener la opción para que el usuario pueda ver:

- Sus órdenes generadas
- El detalle de los artículos comprados
- El status de su pedido

## Condiciones

La comunicación entre el frontend y el backend debe ser por medio de microservicios con operaciones para:

1. Crear
2. Modificar
3. Consultar
4. Borrar

### Tecnologías

- Frontend: React JS
- Backend: Java Springboot
- Base de datos: MariaDB/MySQL
- Utilización de JWT

El código se debe subir a un repositorio de GitLab público, creando una rama con el nombre:

`APP-{Nombre}`

Se deberá realizar una demo mostrando:

- La solución funcional
- La estructura a nivel de código

---

# Sección 3: Conocimientos

## 1.

En Java, ¿qué hace la palabra `synchronized`?

`synchronized` garantiza **exclusión mutua** y **visibilidad de memoria** entre hilos usando el
*monitor* (candado intrínseco) de un objeto:

- **Exclusión mutua:** solo un hilo a la vez puede ejecutar código sincronizado sobre el mismo
  monitor. Los demás hilos quedan bloqueados hasta que se libera.
- **Visibilidad (relación *happens-before*):** al salir del bloque, los cambios hechos por un hilo
  quedan visibles para el siguiente hilo que adquiera el mismo monitor. Esto evita leer valores
  obsoletos que estaban en la caché de la CPU.
- Es **reentrante:** un hilo que ya tiene el candado puede volver a entrar sin bloquearse.


En otras palabras synchronized en Java controla el acceso concurrente a métodos o bloques de código. Evita que varios hilos ejecuten simultáneamente una sección crítica sobre el mismo recurso compartido, ayuda a prevenir condiciones de carrera y problemas de consistencia de datos. synchronized hace que por un lock/monitor un hilo pueda escribir a la vez

## 2.

¿Qué es la inversión de control (IoC), y cómo se implementa en Spring?

ioc es el principio framework que controla parte del flujo o el ciclo de vida, un objeto **no crea ni busca sus
dependencias**: las recibe de un contenedor externo. El control del ciclo de vida y del cableado
pasa del código de la aplicación al framework (principio de Hollywood: "no nos llames, nosotros
te llamamos"). Su forma más común es la **inyección de dependencias (DI)**.

En spring el spring container (`ApplicationContext`, que extiende
`BeanFactory`) es quien administra los beans (clases), y el scope le dice como se comportara ese objeto del bean, si se creara solo una vez por app, si por se creara por cada request, si se creara por cada inyeccion 

1. **Registro de *beans*:** con anotaciones estereotipo (`@Component`, `@Service`, `@Repository`,
   `@Controller`/`@RestController`) detectadas por *component scan*, con métodos `@Bean` dentro de
   clases `@Configuration`, o con XML en proyectos antiguos.
2. **Inyección:** por **constructor** (la recomendada, porque permite dependencias `final` e
   inmutables), por *setter* o por campo, usando `@Autowired` o implícitamente cuando hay un solo
   constructor.
3. **Ciclo de vida y alcance:** el contenedor crea, configura y destruye los *beans*
   (`@PostConstruct`, `@PreDestroy`) y maneja *scopes*: `singleton` (por defecto), `prototype`,
   `request`, `session`.


## 3.

Describa el propósito de las siguientes anotaciones:

- `@SpringBootApplication`
- `@RestController`
- `@GetMapping`
- `@Autowired`



- SpringBootApplication : El el objeto main de la aplicacion, donde se construye la aplicacion como tal con todos los objetos requeridos, arranca y configura spring boot, auto configuracion y escaneo de componentes
- RestController: un controller como tal (@controller) en su definicion esta pensando para mvc, para arrojar en si una vista, mezclado con @ResponseBody se le dice que lo que retorne la funcion debe de ser parseado a json (configurable) e inyectado al body del response de la peticion, RestController dice eso
- GetMapping: Es la forma de mapear un endpoint a un metodo, existen sus versiones por cada metodo http
- Autowired: para decirle a spring que este atributo es inyectado aunque ahora se prefiere que todo venga por constructor



## 4.

En HTML, ¿cómo empieza un documento de HTML5?


Con la declaración **`<!DOCTYPE html>`** en la primera línea. Indica al navegador que interprete
la página en **modo estándar** (sin *quirks mode*). A diferencia de HTML 4 o XHTML, no requiere
DTD ni versión. Un esqueleto mínimo correcto:

```html
<!DOCTYPE html>
<html lang="es">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Título de la página</title>
</head>
<body>
  <h1>Hola mundo</h1>
</body>
</html>
```

La etiqueta `meta viewport` es indispensable para que el diseño *responsive* funcione en móviles.


## 5.

Explica la diferencia entre `var`, `let` y `const`. Da un ejemplo de cuándo usarías cada uno.


las 3 guardan datos, pero de forma diferente, var puede redefinirse y cada que lo hace reinicia el valor al setearse nuevamente, let se define una vez y su valor es actualizable y solo se reinicia por decicion, cons (constante) solo se define una vez y es inmutable, sin emabargo si se peuden mutar los valores dentro del const

La diferencia entre let y var es que var global en el archivo/clase/metodo y se puede redefinir, let puede ser redefinida pero solo sobreescribiendo el valor no creando el nuevo espacio en ram y es mas local

const es como let pero su valor no es mutable 

```js
// const: valores que no se reasignan (debería ser la opción por defecto)
const IVA = 0.13;
const carrito = [];
carrito.push({ id: 1 }); // válido: se modifica el arreglo, no la referencia
// carrito = [];         // TypeError: Assignment to constant variable

// let: valores que cambian, como contadores, acumuladores o variables de ciclo
let total = 0;
for (let i = 0; i < carrito.length; i++) {
  total += carrito[i].precio;
}

// var: solo en código heredado. Su ámbito de función provoca errores sutiles:
for (var j = 0; j < 3; j++) setTimeout(() => console.log(j)); // imprime 3, 3, 3
for (let k = 0; k < 3; k++) setTimeout(() => console.log(k)); // imprime 0, 1, 2
```

Regla práctica: **`const` por defecto, `let` cuando se necesita reasignar y evitar `var`**.

## 6.

Explica qué es JSX y por qué se utiliza en React.

JSX es una extensión de sintaxis de JavaScript utilizada en React que permite describir la interfaz usando una sintaxis similar a HTML. El código JSX es transformado durante la compilación a llamadas de JavaScript que React utiliza para construir y actualizar la interfaz. Se utiliza porque hace que los componentes sean más legibles y permite combinar fácilmente la estructura visual con expresiones y lógica de JavaScript.


Diferencias con HTML: `className` en lugar de `class`, `htmlFor` en lugar de `for`, eventos en
*camelCase* (`onClick`), etiquetas siempre cerradas (`<img />`) y un único elemento raíz (o un
*fragment* `<>...</>`). JSX es opcional (se podría usar `createElement` directamente), pero es el
estándar de facto.


## 7.

¿Qué evalúa la siguiente expresión regular?

```regex
^[0-9]+$
```


Valida que **toda la cadena esté formada únicamente por uno o más dígitos del 0 al 9**:

| Parte | Significado |
|---|---|
| `^` | inicio de la cadena |
| `[0-9]` | un carácter que sea un dígito del 0 al 9 |
| `+` | una o más repeticiones del elemento anterior (no acepta cadena vacía) |
| `$` | fin de la cadena |

| Entrada | ¿Coincide? |
|---|---|
| `"123"`, `"007"`, `"5"` | Sí |
| `""` (vacía) | No (el `+` exige al menos un dígito) |
| `"12a"`, `"-5"`, `"1.5"`, `" 12"`, `"1 000"` | No |

Uso típico: validar códigos o campos numéricos sin signo ni decimales (número de orden, código
postal, cantidad). Detalle: en algunos motores (Python, PCRE), `$` también coincide antes de un
salto de línea final, así que `"123\n"` pasaría. Para una validación estricta conviene usar `\z`
o métodos de coincidencia completa (`re.fullmatch` en Python, `String.matches` en Java). En modo
multilínea (`m`), `^` y `$` aplican a cada línea.

## 8.

¿Cuál es la diferencia entre:

- POST
- GET
- PUT
- PATCH
| Método | Propósito | ¿Seguro? (no modifica) | ¿Idempotente? | ¿Lleva cuerpo? | Respuesta típica |
|---|---|---|---|---|---|
| **GET** | **Consultar** un recurso o colección | Sí | Sí | No, pero puede llevar parametros de consulta en uri | 200 OK |
| **POST** | **Crear** un recurso subordinado o ejecutar un proceso; el servidor asigna el identificador | No | **No** (dos POST pueden crear dos recursos) | Sí | 201 Created + `Location` |
| **PUT** | **Reemplazar por completo** el recurso en una URI conocida (o crearlo en esa URI) | No | **Sí** (repetirlo deja el mismo estado) | Sí, con la representación completa | 200 OK / 204 No Content |
| **PATCH** | **Modificar parcialmente** un recurso: solo los campos enviados | No | No necesariamente (depende del formato; JSON Merge Patch suele serlo) | Sí, con los cambios | 200 OK / 204 No Content |

- **GET vs POST:** GET obtiene datos sin efectos secundarios, se puede cachear y sus parámetros van
  en la URL. POST envía datos en el cuerpo para crear o procesar algo y no se cachea.
- **PUT vs PATCH:** con PUT, si se omite un campo, se entiende que se quiere borrar o dejar en su
  valor por defecto, porque se envía el recurso completo. Con PATCH solo se envía lo que cambia.

Ejemplos en el caso práctico: `POST /api/orders` crea una orden, `GET /api/orders/{n}` la
consulta, `PUT /api/users/me` reemplaza el perfil completo y `PATCH /api/users/me/shipping-address`
cambia solo la dirección desde el checkout..


## 9.

En Node.js, ¿qué hace la instrucción `async`?

`async` declara una **función asíncrona** esto significa que no se sabra cuanto tardara en ejecutarse/procesarse esa funcion ya que depende de un proceso fuera del even loop: leer en base, fetch, leer en disco etc:

1. **Siempre devuelve una `Promise`:** el valor retornado resuelve la promesa y una excepción la
   rechaza.
2. **Habilita `await`** dentro de la función. `await` suspende la ejecución *de esa función* (no
   del hilo) hasta que la promesa se resuelve. Mientras tanto, el *event loop* sigue atendiendo
   otras tareas.
3. Permite escribir código asíncrono con estilo secuencial y manejar errores con `try/catch`, en
   lugar de encadenar `.then()` o anidar *callbacks*.



## 10.

¿Qué es el event loop de Node.js y en qué ayuda?

El **event loop** es el mecanismo (implementado por **libuv**) que permite a Node.js hacer
**operaciones de E/S no bloqueantes con un solo hilo de JavaScript**. Las operaciones lentas (red,
disco, base de datos) se delegan al sistema operativo o a un *thread pool*. Cuando terminan, sus
*callbacks* se encolan y el event loop los ejecuta uno a uno cuando la pila de llamadas queda vacía.


```js
console.log('1');
setTimeout(() => console.log('4 (timer)'), 0);
Promise.resolve().then(() => console.log('3 (microtarea)'));
console.log('2');
// Salida: 1, 2, 3 (microtarea), 4 (timer)
```

En qué ayuda: permite atender miles de conexiones concurrentes con poca memoria (no hay un hilo por petición), lo que lo hace ideal para cargas intensivas en E/S: APIs, gateways, streaming y tiempo real (WebSockets). Limitación: una tarea intensiva en CPU (cálculos pesados, JSON enormes) bloquea el loop y detiene todas las peticiones. Esas tareas se envían a worker_threads, a procesos separados o a otro servicio.


## 11.

¿Es recomendable implementar bases de datos en contenedores?

¿Por qué?


**Depende del entorno:**

**Sí, en desarrollo, pruebas y CI.** Es recomendable e plausible

**En producción, es posible, pero con cuidado.** Los contenedores son efímeros y las bases de datos
tienen estado, el problema principal son los datos, no son replicables de la misma forma que una aplicacion, se pueden crear volumenes de datos pero lo mas recomendable es tener la db afuera en algo por el prov de nube o en un servidor de datos


## 12.

Indiqué a qué se refiere OWASP.


**OWASP** (*Open Worldwide Application Security Project*, antes *Open Web Application Security
Project*) es una **fundación sin fines de lucro y comunidad abierta** que produce recursos
gratuitos para mejorar la seguridad del software. Sus proyectos más conocidos:

- **OWASP Top 10:** documento de referencia con los riesgos más críticos de las aplicaciones web.
  La edición 2021 incluye: A01 Control de acceso roto, A02 Fallas criptográficas, A03 Inyección,
  A04 Diseño inseguro, A05 Configuración de seguridad incorrecta, A06 Componentes vulnerables y
  desactualizados, A07 Fallas de identificación y autenticación, A08 Fallas de integridad de
  software y datos, A09 Fallas de registro y monitoreo, A10 SSRF. La edición 2025 reorganiza la
  lista e incorpora, entre otros, las fallas en la cadena de suministro de software.
- **OWASP API Security Top 10**, **ASVS** (estándar de verificación), **Cheat Sheet Series**,
  **SAMM** (modelo de madurez), **Dependency-Check** y **Juice Shop** (aplicación vulnerable para
  practicar).


---

# Sección 4: Conocimiento de frameworks

## 1.

Escribe un controlador REST en Spring Boot que maneje una solicitud GET en la ruta:

```text
/greeting
```


y devuelva un mensaje de saludo.

El mensaje debe incluir un parámetro de consulta opcional:

```text
name
```


```java
package com.ejemplo.greeting;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetingController {

    private static final String DEFAULT_NAME = "Mundo";

    @GetMapping("/greeting")
    public Greeting greeting(@RequestParam(required = false) String name) {
        String who = (name == null || name.isBlank()) ? DEFAULT_NAME : name.trim();
        return new Greeting("¡Hola, " + who + "!");
    }

    /** Se serializa automáticamente a JSON gracias a @RestController. */
    public record Greeting(String message) {
    }
}
```

Comportamiento:

| Petición | Respuesta (`200 OK`, `application/json`) |
|---|---|
| `GET /greeting` | `{"message":"¡Hola, Mundo!"}` |
| `GET /greeting?name=Tigo` | `{"message":"¡Hola, Tigo!"}` |
| `GET /greeting?name=%20%20` | `{"message":"¡Hola, Mundo!"}` (un nombre en blanco se trata como ausente) |

## 2.

Escriba el Dockerfile para crear un contenedor con la última versión de Nginx con un `index.html` únicamente.


```dockerfile
FROM nginx:latest

# Se eliminan los archivos por defecto para que el contenedor sirva solo nuestro index.html.
RUN rm -rf /usr/share/nginx/html/*
# De nuestro host hacia el contenedor
COPY index.html /usr/share/nginx/html/index.html

EXPOSE 80
# La imagen base ya define: CMD ["nginx", "-g", "daemon off;"]
```

```bash
cd seccion-4/nginx-index
docker build -t nginx-index .
docker run --rm -p 8088:80 nginx-index
# http://localhost:8088 muestra el index.html
```
---

# Sección 5: Gestión de Proyectos

## 1.

¿En qué momento inicia un sprint en metodología Scrum?

Un Sprint **inicia con la *Sprint Planning*** y **comienza inmediatamente después de que termina el
Sprint anterior**, sin pausas entre uno y otro


## 2.

Mencione 3 roles involucrados en metodología Scrum.

| Rol | Responsabilidad principal |
|---|---|
| **Product Owner** | Maximizar el valor del producto. Gestiona y prioriza el *Product Backlog*, define el Objetivo del Producto y es la voz del negocio y de los usuarios. Es una sola persona, no un comité. |
| **Scrum Master** | Asegurar que Scrum se entienda y se aplique. Es un líder al servicio del equipo: facilita los eventos, elimina impedimentos, entrena en autogestión y ayuda a la organización a adoptar Scrum. |
| **Developers** (equipo de desarrollo) | Crear un incremento utilizable y que cumpla la *Definition of Done* en cada Sprint. Planifican el *Sprint Backlog*, se autogestionan y son multifuncionales (desarrollo, QA, diseño, etc.). |

Los *stakeholders* (clientes, usuarios, patrocinadores) participan, por ejemplo en la *Sprint
Review*, pero no forman parte del Equipo Scrum.

## 3.

¿Según el PMBOK qué significa SPI y qué representa su valor igual a 1?

SPI (Schedule Performance Index) mide el desempeño del cronograma y se calcula como EV / PV. El EV y el PV suelen expresarse usando el valor presupuestado del trabajo como unidad de comparación. Un SPI = 1 significa que el trabajo realizado coincide con lo planificado para ese momento; mayor a 1 indica adelanto y menor a 1 atraso.