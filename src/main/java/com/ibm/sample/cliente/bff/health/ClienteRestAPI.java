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
		int ponto=0;
		try
		{
			logger.debug("Criando mensagem sintetica para teste da API Rest");
			cliente.setCpf(123L);
			cliente.setNome("CLIENTE SINTETICO - HEALTH CHECK");
			cliente.setNumero(10);
			cliente.setNasc(new java.util.Date());
			cliente.setCidade("Cidade 1");
			cliente.setComplemento(" ");
			cliente.setLogradouro("Rua teste");
			cliente.setMae("Mae teste");
			cliente.setUf("SP");
			cliente.setCep("123442");
			logger.debug("Fazendo chamada Post na API Rest para gravar cliente sintetico");
			clienteRestHealth.postForObject(urlClienteRest,cliente, RetornoCliente.class);
			logger.debug("POST realizado com sucesso, aguardando 200 ms para fazer a consulta");
			ponto=1;
			//aguarda o processamento asincrino
			Thread.sleep(200);
			clienteRestHealth.getForObject(urlClienteRest + "/" + cliente.getCpf(), RetornoCliente.class);
			logger.debug("Consulta do cliente recem cadastrado efetuada com sucesso! Testando a deleção");
			ponto=2;
			clienteRestHealth.delete(urlClienteRest + "/" + cliente.getCpf());
			logger.debug("Deleção do cliente sintetico efetuada com sucesso");
			ponto=3;
			logger.debug("ClienteRestAPI esta saudável");
			return Health.up().build();
			
		}
		catch (Exception e)
		{
			logger.error("ClienteRestAPI não esta saudável. Falha ao validar a saúde da RestAPI de Cliente: " + e.getMessage());
			String mensagem = "Falha na inclusão de novos clientes: " + e.getMessage();
			if (ponto >=1)
			{
				mensagem = "Falha na consulta de um cliente: " + e.getMessage();
			}
			else if (ponto >2)
			{
				mensagem = "Falha na exclusão de um cliente: " + e.getMessage();
			}
			logger.error("ClienteRestAPI não saudável: " + mensagem);
			return Health.down().withDetail("Cliente-BFF Não saudável", mensagem).build();
		}
	}
	
}
