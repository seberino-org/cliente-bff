package com.ibm.sample.cliente.bff;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;


@SpringBootApplication
public class ClienteBffApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClienteBffApplication.class, args);
	}

	@Bean
	public RestTemplate clienteRest(RestTemplateBuilder builder) {
		return builder.build();
	}
	
	
}
