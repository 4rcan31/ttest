package com.sportshop.catalog.config;

import java.time.Duration;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Sirve las imágenes del catálogo con caché HTTP larga (en producción irían en un bucket + CDN). */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    public static final String IMAGES_PATH = "/api/products/images/";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(IMAGES_PATH + "**")
                .addResourceLocations("classpath:/static/images/products/")
                .setCacheControl(CacheControl.maxAge(Duration.ofDays(7)).cachePublic());
    }
}
