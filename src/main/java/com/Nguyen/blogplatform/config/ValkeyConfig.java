package com.Nguyen.blogplatform.config;

import glide.api.GlideClient;
import glide.api.models.configuration.GlideClientConfiguration;
import glide.api.models.configuration.NodeAddress;
import glide.api.models.exceptions.GlideException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import java.util.concurrent.ExecutionException;

@Configuration
public class ValkeyConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Bean(destroyMethod = "close")
    public GlideClient glideClient() throws ExecutionException, InterruptedException {

        GlideClientConfiguration config = GlideClientConfiguration.builder()
                .address(NodeAddress.builder()
                        .host(host)
                        .port(port)
                        .build()
                )
                .build();

        // IMPORTANT: resolve CompletableFuture
        return GlideClient.createClient(config).get();
    }
}
