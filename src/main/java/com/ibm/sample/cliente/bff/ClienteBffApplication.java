package com.ibm.sample.cliente.bff;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.client.RestTemplate;

import com.ibm.sample.cliente.bff.dto.Cliente;

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
