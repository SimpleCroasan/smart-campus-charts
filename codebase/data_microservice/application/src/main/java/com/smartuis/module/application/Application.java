package com.smartuis.module.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication(scanBasePackages = "com.smartuis.module")
@PropertySource({
        "classpath:application/application.properties",
        "classpath:persistence/application.properties",
})
@EnableMongoRepositories(basePackages = "com.smartuis.module.persistence")
@ComponentScan(basePackages = {"com.smartuis.module", "com.module.service.impl"})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
