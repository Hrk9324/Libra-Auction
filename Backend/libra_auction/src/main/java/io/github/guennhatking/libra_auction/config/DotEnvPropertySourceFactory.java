package io.github.guennhatking.libra_auction.config;

import java.io.IOException;
import java.util.Properties;

import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

public class DotEnvPropertySourceFactory implements PropertySourceFactory {
    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource resource) throws IOException {
        Properties props = new Properties();
        if (resource != null && resource.getResource().exists()) {
            props.load(resource.getInputStream());
        }
        return new PropertiesPropertySource(name != null ? name : "dotenv", props);
    }
}
