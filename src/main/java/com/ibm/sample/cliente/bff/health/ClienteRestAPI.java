package com.ibm.sample.cliente.bff.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.ibm.sample.cliente.bff.dto.Cliente;
import com.ibm.sample.cliente.bff.dto.RetornoCliente;

@Component
public class ClienteRestAPI implements HealthIndicator {

	Logger logger = LoggerFactory.getLogger(ClienteRestAPI.class);

	@Autowired
	private RestTemplate clienteRestHealth;
	
	private Cliente cliente = new Cliente();
	
	@Value("${cliente-rest.url}")
	private String urlClienteRest; 	
	
	@Override
	public Health health() {
		logger.debug("[health] ClienteRestAPI");
		try
		{

			clienteRestHealth.getForObject(urlClienteRest + "/17956462843" , RetornoCliente.class);
			logger.debug("ClienteRestAPI is not health");
			return Health.up().build();
			
		}
		catch (Exception e)
		{
			logger.error("ClienteRestAPI is not Health. Error to validate RestAPI of Client: " + e.getMessage());
			return Health.down().withDetail("Cliente-Rest is not Health: ",e.getMessage()).build();
		}
	}
	
}
