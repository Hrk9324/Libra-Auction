package io.github.guennhatking.libra_auction.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = "file:${user.dir}/.env", factory = DotEnvPropertySourceFactory.class, ignoreResourceNotFound = true)
@EnableConfigurationProperties
public class EnvironmentConfig {
}
