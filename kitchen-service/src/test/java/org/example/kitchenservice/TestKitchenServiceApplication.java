package org.example.kitchenservice;

import org.springframework.boot.SpringApplication;

public class TestKitchenServiceApplication {

    public static void main(String[] args) {
        SpringApplication.from(KitchenServiceApplication::main)
                .with(TestcontainersConfiguration.class)
                .run(args);
    }
}
