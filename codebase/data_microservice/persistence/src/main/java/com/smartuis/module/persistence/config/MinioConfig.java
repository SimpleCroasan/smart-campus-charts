package com.smartuis.module.persistence.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    @Value("${minio.url}")
    private String url;
    @Value("${minio.acces.key}")
    private String acces_key;
    @Value("${minio.secret.key}")
    private String secret_key;

    @Bean
    MinioClient minioClient(){
        return MinioClient.builder()
                        .endpoint(url)
                        .credentials(acces_key, secret_key)
                        .build();
    }
}
