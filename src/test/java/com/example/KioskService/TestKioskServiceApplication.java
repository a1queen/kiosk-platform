package com.example.KioskService;

import org.springframework.boot.SpringApplication;

public class TestKioskServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(KioskServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
