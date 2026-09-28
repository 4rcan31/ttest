package com.ejemplo.greeting;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * GET /greeting            -> {"message":"¡Hola, Mundo!"}
 * GET /greeting?name=Tigo  -> {"message":"¡Hola, Tigo!"}
 */
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
