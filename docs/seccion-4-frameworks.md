# Sección 4: Conocimiento de frameworks

Ambas respuestas tienen código ejecutable en [`seccion-4/`](../seccion-4) y fueron verificadas:
el controlador tiene pruebas automatizadas y la imagen de nginx se construyó y se ejecutó.

## 1. Controlador REST en Spring Boot: `GET /greeting` con parámetro opcional `name`

Proyecto completo: [`seccion-4/greeting-api`](../seccion-4/greeting-api)

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

La forma más corta sería `@RequestParam(defaultValue = "Mundo") String name`. Aquí se usa
`required = false` para tratar también el caso de un parámetro vacío o con solo espacios.

Ejecutar y probar:

```bash
cd seccion-4/greeting-api
mvn test              # 3 pruebas con MockMvc (@WebMvcTest)
mvn spring-boot:run   # luego: curl "http://localhost:8080/greeting?name=Tigo"
```

## 2. Dockerfile con la última versión de nginx y únicamente un `index.html`

Archivos: [`seccion-4/nginx-index`](../seccion-4/nginx-index)

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

Notas:

- `nginx:latest` cumple el requisito de "la última versión". En producción recomiendo **fijar una
  versión** (por ejemplo `nginx:1.29-alpine`) o incluso su *digest*, para tener builds
  reproducibles y actualizar de forma controlada. El frontend del caso práctico usa
  `nginx:1.29-alpine`.
- El `rm -rf` elimina el `50x.html` que trae la imagen base, así el contenedor sirve
  **únicamente** el `index.html` solicitado.
