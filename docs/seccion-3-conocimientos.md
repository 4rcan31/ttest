# Sección 3: Conocimientos

## 1. En Java, ¿qué hace la palabra `synchronized`?

`synchronized` garantiza **exclusión mutua** y **visibilidad de memoria** entre hilos usando el
*monitor* (candado intrínseco) de un objeto:

- **Exclusión mutua:** solo un hilo a la vez puede ejecutar código sincronizado sobre el mismo
  monitor. Los demás hilos quedan bloqueados hasta que se libera.
- **Visibilidad (relación *happens-before*):** al salir del bloque, los cambios hechos por un hilo
  quedan visibles para el siguiente hilo que adquiera el mismo monitor. Esto evita leer valores
  obsoletos que estaban en la caché de la CPU.
- Es **reentrante:** un hilo que ya tiene el candado puede volver a entrar sin bloquearse.


En otras palabras synchronized en Java controla el acceso concurrente a métodos o bloques de código. Evita que varios hilos ejecuten simultáneamente una sección crítica sobre el mismo recurso compartido, ayuda a prevenir condiciones de carrera y problemas de consistencia de datos. synchronized hace que por un lock/monitor un hilo pueda escribir a la vez


Formas de uso:

```java
public class Contador {
    private int valor;
    private final Object lock = new Object();

    // 1) Método de instancia: bloquea sobre "this"
    public synchronized void incrementar() { valor++; }

    // 2) Método estático: bloquea sobre Contador.class
    public static synchronized void metodoEstatico() { /* ... */ }

    // 3) Bloque: bloquea sobre un objeto específico y reduce la sección crítica
    public void incrementarConBloque() {
        synchronized (lock) {
            valor++;
        }
    }
}
```

Sin `synchronized`, `valor++` (leer, sumar, escribir) no es atómico y dos hilos pueden perder
incrementos (*race condition*).

Consideraciones: tiene costo por la contención y puede causar *deadlocks* si se adquieren
candados en distinto orden. Para casos específicos existen alternativas de `java.util.concurrent`:
`AtomicInteger`, `ReentrantLock` (con *timeout* o equidad), `ConcurrentHashMap`. Con los *virtual
threads* de Java 21, un bloque `synchronized` con operaciones bloqueantes "ancla" (*pinning*) el
hilo virtual a su hilo de plataforma. Java 24 corrigió esto (JEP 491).

## 2. ¿Qué es la inversión de control (IoC) y cómo se implementa en Spring?

**Inversión de control** es el principio por el cual un objeto **no crea ni busca sus
dependencias**: las recibe de un contenedor externo. El control del ciclo de vida y del cableado
pasa del código de la aplicación al framework (principio de Hollywood: "no nos llames, nosotros
te llamamos"). Su forma más común es la **inyección de dependencias (DI)**.

Beneficios: bajo acoplamiento (se programa contra interfaces), facilidad para probar (se inyectan
*mocks*) y configuración externalizada (se cambia una implementación sin tocar el código cliente).

**En Spring** lo implementa el **contenedor IoC** (`ApplicationContext`, que extiende
`BeanFactory`):

1. **Registro de *beans*:** con anotaciones estereotipo (`@Component`, `@Service`, `@Repository`,
   `@Controller`/`@RestController`) detectadas por *component scan*, con métodos `@Bean` dentro de
   clases `@Configuration`, o con XML en proyectos antiguos.
2. **Inyección:** por **constructor** (la recomendada, porque permite dependencias `final` e
   inmutables), por *setter* o por campo, usando `@Autowired` o implícitamente cuando hay un solo
   constructor.
3. **Ciclo de vida y alcance:** el contenedor crea, configura y destruye los *beans*
   (`@PostConstruct`, `@PreDestroy`) y maneja *scopes*: `singleton` (por defecto), `prototype`,
   `request`, `session`.
4. **Resolución de ambigüedad:** `@Qualifier` y `@Primary` cuando hay varias implementaciones.

```java
public interface NotificadorService { void enviar(String mensaje); }

@Service
class CorreoNotificador implements NotificadorService {
    public void enviar(String mensaje) { /* SMTP */ }
}

@Service
class PedidoService {
    private final NotificadorService notificador;

    // Spring inyecta la implementación; PedidoService no hace "new CorreoNotificador()"
    PedidoService(NotificadorService notificador) {
        this.notificador = notificador;
    }
}
```

En el caso práctico, por ejemplo, `OrderService` recibe `CatalogClient`, los repositorios y el
`Clock` por constructor, y en las pruebas se reemplaza `CatalogClient` por un *mock* con
`@MockitoBean`.

## 3. Propósito de `@SpringBootApplication`, `@RestController`, `@GetMapping` y `@Autowired`

| Anotación | Propósito |
|---|---|
| `@SpringBootApplication` | Marca la clase principal de una aplicación Spring Boot. Combina tres anotaciones: **`@SpringBootConfiguration`** (una `@Configuration`), **`@EnableAutoConfiguration`** (configura *beans* automáticamente según las dependencias del *classpath*, como el servidor web embebido, el DataSource o Jackson) y **`@ComponentScan`** (busca componentes en el paquete de la clase y en sus subpaquetes). Se usa junto con `SpringApplication.run(...)`. |
| `@RestController` | Equivale a `@Controller` + `@ResponseBody`. Declara una clase que atiende peticiones HTTP y cuyos métodos devuelven datos que se **serializan directamente en el cuerpo de la respuesta** (normalmente JSON con Jackson), en lugar de nombres de vistas. |
| `@GetMapping` | Atajo de `@RequestMapping(method = RequestMethod.GET)`. Asocia las peticiones **HTTP GET** a una ruta con un método del controlador. Existen equivalentes para otros verbos: `@PostMapping`, `@PutMapping`, `@PatchMapping` y `@DeleteMapping`. |
| `@Autowired` | Marca un **punto de inyección de dependencias** (constructor, *setter* o campo). Spring lo resuelve **por tipo** desde el contenedor. Si la clase tiene un único constructor, la anotación es opcional (desde Spring 4.3). `@Autowired(required = false)` hace la dependencia opcional. |

```java
@SpringBootApplication
public class TiendaApplication {
    public static void main(String[] args) { SpringApplication.run(TiendaApplication.class, args); }
}

@RestController
@RequestMapping("/api/products")
class ProductController {
    private final ProductService service;

    @Autowired // opcional con un único constructor
    ProductController(ProductService service) { this.service = service; }

    @GetMapping("/{id}")
    ProductResponse getById(@PathVariable Long id) { return service.getById(id); }
}
```

## 4. En HTML, ¿cómo empieza un documento de HTML5?

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

## 5. Diferencia entre `var`, `let` y `const`, con ejemplos de uso

| Característica | `var` | `let` | `const` |
|---|---|---|---|
| Ámbito | de **función** (o global) | de **bloque** `{ }` | de **bloque** `{ }` |
| *Hoisting* | se eleva y se inicializa con `undefined` | se eleva, pero queda en la *Temporal Dead Zone* (usarla antes de declararla lanza `ReferenceError`) | igual que `let` |
| Redeclarar en el mismo ámbito | permitido | error | error |
| Reasignar | sí | sí | **no** (la referencia es constante, aunque un objeto o arreglo se puede modificar internamente) |
| En el ámbito global | crea una propiedad de `window` | no | no |

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

## 6. ¿Qué es JSX y por qué se utiliza en React?

**JSX** (*JavaScript XML*) es una extensión de sintaxis de JavaScript que permite escribir
marcado parecido a HTML dentro del código. No es HTML ni lo entiende el navegador: un compilador
(Babel, esbuild o SWC, integrado en Vite) lo transforma en llamadas a funciones que crean
elementos de React:

```jsx
const saludo = <h1 className="titulo">Hola, {usuario.nombre}</h1>;
// se compila (JSX transform moderno) aproximadamente a:
const saludo = jsx('h1', { className: 'titulo', children: ['Hola, ', usuario.nombre] });
```

Por qué se utiliza:

- **UI declarativa y legible:** se describe *qué* debe verse según el estado, y la estructura se
  parece al resultado final.
- **Todo el poder de JavaScript:** expresiones entre `{ }`, `map` para listas, condicionales y
  composición de componentes como si fueran etiquetas (`<ProductCard product={p} />`).
- **Seguridad:** React **escapa por defecto** los valores interpolados, lo que previene XSS (salvo
  que se use `dangerouslySetInnerHTML` de forma explícita).
- **Herramientas:** errores de compilación, autocompletado y verificación de tipos con TypeScript.

Diferencias con HTML: `className` en lugar de `class`, `htmlFor` en lugar de `for`, eventos en
*camelCase* (`onClick`), etiquetas siempre cerradas (`<img />`) y un único elemento raíz (o un
*fragment* `<>...</>`). JSX es opcional (se podría usar `createElement` directamente), pero es el
estándar de facto.

## 7. ¿Qué evalúa la expresión regular `^[0-9]+$`?

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

## 8. ¿Cuál es la diferencia entre POST/GET y PUT/PATCH?

Los cuatro son métodos HTTP con semánticas distintas:

| Método | Propósito | ¿Seguro? (no modifica) | ¿Idempotente? | ¿Lleva cuerpo? | Respuesta típica |
|---|---|---|---|---|---|
| **GET** | **Consultar** un recurso o colección | Sí | Sí | No | 200 OK |
| **POST** | **Crear** un recurso subordinado o ejecutar un proceso; el servidor asigna el identificador | No | **No** (dos POST pueden crear dos recursos) | Sí | 201 Created + `Location` |
| **PUT** | **Reemplazar por completo** el recurso en una URI conocida (o crearlo en esa URI) | No | **Sí** (repetirlo deja el mismo estado) | Sí, con la representación completa | 200 OK / 204 No Content |
| **PATCH** | **Modificar parcialmente** un recurso: solo los campos enviados | No | No necesariamente (depende del formato; JSON Merge Patch suele serlo) | Sí, con los cambios | 200 OK / 204 No Content |

- **GET vs POST:** GET obtiene datos sin efectos secundarios, se puede cachear y sus parámetros van
  en la URL. POST envía datos en el cuerpo para crear o procesar algo y no se cachea.
- **PUT vs PATCH:** con PUT, si se omite un campo, se entiende que se quiere borrar o dejar en su
  valor por defecto, porque se envía el recurso completo. Con PATCH solo se envía lo que cambia.

Ejemplos en el caso práctico: `POST /api/orders` crea una orden, `GET /api/orders/{n}` la
consulta, `PUT /api/users/me` reemplaza el perfil completo y `PATCH /api/users/me/shipping-address`
cambia solo la dirección desde el checkout.

## 9. En Node.js, ¿qué hace la instrucción `async`?

`async` declara una **función asíncrona**:

1. **Siempre devuelve una `Promise`:** el valor retornado resuelve la promesa y una excepción la
   rechaza.
2. **Habilita `await`** dentro de la función. `await` suspende la ejecución *de esa función* (no
   del hilo) hasta que la promesa se resuelve. Mientras tanto, el *event loop* sigue atendiendo
   otras tareas.
3. Permite escribir código asíncrono con estilo secuencial y manejar errores con `try/catch`, en
   lugar de encadenar `.then()` o anidar *callbacks*.

```js
async function obtenerPedido(id) {
  try {
    const pedido = await db.pedidos.findById(id);         // espera sin bloquear el hilo
    const cliente = await db.clientes.findById(pedido.clienteId);
    return { ...pedido, cliente };                        // resuelve la Promise
  } catch (error) {
    throw new Error(`No se pudo obtener el pedido: ${error.message}`); // rechaza la Promise
  }
}

// Operaciones independientes en paralelo:
const [productos, categorias] = await Promise.all([api.productos(), api.categorias()]);
```

Es azúcar sintáctico sobre las promesas. En módulos ES también existe el *top-level await*.

## 10. ¿Qué es el event loop de Node.js y en qué ayuda?

El **event loop** es el mecanismo (implementado por **libuv**) que permite a Node.js hacer
**operaciones de E/S no bloqueantes con un solo hilo de JavaScript**. Las operaciones lentas (red,
disco, base de datos) se delegan al sistema operativo o a un *thread pool*. Cuando terminan, sus
*callbacks* se encolan y el event loop los ejecuta uno a uno cuando la pila de llamadas queda vacía.

Fases de cada vuelta: **timers** (`setTimeout`/`setInterval`) → *pending callbacks* → *idle/prepare*
→ **poll** (espera y procesa E/S) → **check** (`setImmediate`) → *close callbacks*. Entre cada
*callback* se vacían las **microtareas** (`process.nextTick` y luego las promesas).

```js
console.log('1');
setTimeout(() => console.log('4 (timer)'), 0);
Promise.resolve().then(() => console.log('3 (microtarea)'));
console.log('2');
// Salida: 1, 2, 3 (microtarea), 4 (timer)
```

**En qué ayuda:** permite atender **miles de conexiones concurrentes con poca memoria** (no hay un
hilo por petición), lo que lo hace ideal para cargas intensivas en E/S: APIs, *gateways*,
*streaming* y tiempo real (WebSockets). **Limitación:** una tarea intensiva en CPU (cálculos
pesados, JSON enormes) bloquea el loop y detiene todas las peticiones. Esas tareas se envían a
`worker_threads`, a procesos separados o a otro servicio.

## 11. ¿Es recomendable implementar bases de datos en contenedores? ¿Por qué?

**Depende del entorno:**

**Sí, en desarrollo, pruebas y CI.** Es muy recomendable: se levanta la misma versión del motor que
en producción en segundos, de forma reproducible y descartable (en este proyecto, `docker compose`
levanta MariaDB con el esquema de cada servicio; en las pruebas se puede usar Testcontainers).

**En producción, es posible, pero con cuidado.** Los contenedores son efímeros y las bases de datos
tienen estado, así que se necesita:

- **Almacenamiento persistente** fuera del ciclo de vida del contenedor (volúmenes, o
  `PersistentVolumes` con `StatefulSets` en Kubernetes). Si se configura mal, se pueden perder datos.
- **Operadores maduros** (MariaDB Operator, CloudNativePG, Percona) para replicación, *failover*,
  respaldos, restauración a un punto en el tiempo y actualizaciones.
- **Rendimiento de E/S predecible:** límites de recursos, afinidad de nodos y evitar vecinos ruidosos.
- **Monitoreo, seguridad y parches** gestionados por el equipo.

Por eso, para producción suelo recomendar una **base de datos administrada** (Amazon RDS/Aurora,
Azure Database, Cloud SQL): el proveedor se encarga de la alta disponibilidad, los respaldos, los
parches y la conmutación automática, y el equipo se enfoca en el producto. Tener la base de datos
en contenedores en producción se justifica cuando hay razones de costo, portabilidad o
*on-premise* y el equipo domina Kubernetes con estado.

## 12. ¿A qué se refiere OWASP?

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

**Cómo se aplicó en el caso práctico:**

| Riesgo | Mitigación implementada |
|---|---|
| Control de acceso roto / IDOR | El id del usuario siempre se toma del JWT, nunca de la URL. Una orden ajena responde 404. Las rutas `/api/admin/**` requieren el rol ADMIN. Los endpoints internos usan API key y no se publican en el gateway. |
| Fallas criptográficas | Contraseñas con **BCrypt** (costo 12). Tokens de recuperación aleatorios de 256 bits; solo se guarda su hash SHA-256. JWT firmado y con emisor y expiración validados. |
| Inyección | JPA/Criteria con parámetros enlazados. Los comodines del `LIKE` se escapan. React escapa la salida. |
| Fallas de autenticación | Bloqueo temporal después de 5 intentos fallidos, *rate limiting* en nginx, mensajes genéricos que no revelan si un correo existe y tiempo de respuesta constante con un *hash* ficticio. |
| Configuración incorrecta | Cabeceras de seguridad (CSP, X-Frame-Options, nosniff), errores sin *stack traces*, secretos en variables de entorno y usuarios de base de datos con mínimo privilegio por servicio. |
| Diseño inseguro | Reserva de inventario atómica (sin sobreventa), transacciones de compensación y validaciones en el servidor aunque el frontend también valide. |
