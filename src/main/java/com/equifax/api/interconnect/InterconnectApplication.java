package com.equifax.api.interconnect;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.equifax.api.interconnect")
public class InterconnectApplication {
    public static void main(String[] args) {
        SpringApplication.run(InterconnectApplication.class, args);
    }
}
