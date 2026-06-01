package com.eighthours.bovin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class BovinApplication {

    public static void main(String[] args) {
        SpringApplication.run(BovinApplication.class, args);
    }

}
