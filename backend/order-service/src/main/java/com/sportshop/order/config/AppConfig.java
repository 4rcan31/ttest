package com.sportshop.order.config;

import java.net.http.HttpClient;
import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import com.sportshop.common.internal.InternalApiProperties;

@Configuration
public class AppConfig {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    /** Cliente HTTP hacia catalog-service con timeouts explícitos y la API key interna. */
    @Bean
    RestClient catalogRestClient(RestClient.Builder builder, OrderServiceProperties properties,
                                 InternalApiProperties internalApiProperties) {
        OrderServiceProperties.Catalog catalog = properties.catalog();
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(catalog.connectTimeout())
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(catalog.readTimeout());
        return builder
                .baseUrl(catalog.baseUrl())
                .requestFactory(requestFactory)
                .defaultHeader(InternalApiProperties.HEADER, internalApiProperties.apiKey())
                .build();
    }
}
