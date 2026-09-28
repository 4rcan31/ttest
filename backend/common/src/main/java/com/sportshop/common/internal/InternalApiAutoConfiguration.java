package com.sportshop.common.internal;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@AutoConfiguration
@EnableConfigurationProperties(InternalApiProperties.class)
public class InternalApiAutoConfiguration {
}
