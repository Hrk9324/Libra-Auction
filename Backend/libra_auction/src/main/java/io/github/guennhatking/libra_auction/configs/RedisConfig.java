package io.github.guennhatking.libra_auction.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

@Configuration
public class RedisConfig {

    @Bean
    public LettuceConnectionFactory lettuceConnectionFactory() {
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName("redis-18721.c251.east-us-mz.azure.cloud.redislabs.com");
        redisConfig.setPort(18721);
        redisConfig.setUsername("default");
        redisConfig.setPassword("kSqohyHylRNItOORlOzxeR9blWt4Cg3d");
        redisConfig.setDatabase(0);

        return new LettuceConnectionFactory(redisConfig);
    }
}
