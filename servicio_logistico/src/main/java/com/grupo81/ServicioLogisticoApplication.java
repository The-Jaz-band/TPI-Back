package com.grupo81;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ServicioLogisticoApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServicioLogisticoApplication.class, args);
	}

}
