package com.ibm.sample.cliente.bff.health;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import com.ibm.sample.cliente.bff.dto.Cliente;

@Component
public class Kafka implements HealthIndicator {

	Logger logger = LoggerFactory.getLogger(Kafka.class);
	
	@Value("${cliente-kafka-topico}")
	private String topicoCadastro; 
	
	@Value("${delete-cliente-kafka-topico}")
	private String topicoDelete; 
	
	@Value("${spring.kafka.producer.bootstrap-servers}")
	private String kafkaURL;
	
	
	@Value("${spring.kafka.producer.key-serializer}")
	private String keyse;
	
	@Value("${spring.kafka.producer.value-serializer}")
	private String valuese;
	
	@Value("${spring.kafka.producer.ssl.trust-store-password}")
	private String trustStorePassword;
	
	@Value("${spring.kafka.producer.ssl.trust-store-location}")
	private String trustStoreLocation;
	
	@Value("${spring.kafka.producer.ssl.trust-store-type}")
	private String trustStoreType;
	
	@Value("${spring.kafka.producer.security.protocol}")
	private String securityProtocol;
	
	@Value("${spring.kafka.producer.ssl.protocol}")
	private String sslProtocol;
	
	@Value("${spring.kafka.properties.sasl.mechanism}")
	private String saslMechanis;
	
	@Value("${spring.kafka.properties.sasl.jaas.config}")
	private String jaasConfig;
	
	KafkaProducer<String, Cliente> kafka;
	
	private Cliente cliente = new Cliente();
	
	@Override
	public Health health() {
		logger.debug("[health] kafka");
		int errorCode = 0;
		try
		{
			logger.debug("Verificando se já existe conexão aberta com o kafka");
			if (kafka ==null)
			{
				logger.debug("configurando as propriedades para se conectar ao kafka");
				cliente.setCpf(0L);
				cliente.setNome("CLIENTE SINTETICO - HEALTH CHECK");
				cliente.setNumero(0);
				cliente.setNasc(new java.util.Date());
				
				Properties prop = new Properties();
				prop.setProperty("acks","1");
				prop.setProperty("bootstrap.servers",kafkaURL);
				prop.setProperty("key.serializer",keyse);
				prop.setProperty("sasl.jaas.config",jaasConfig);
				prop.setProperty("sasl.mechanism",saslMechanis);
				prop.setProperty("security.protocol",securityProtocol);
				prop.setProperty("ssl.enabled.protocols",sslProtocol);
				prop.setProperty("ssl.truststore.location",trustStoreLocation.substring(5));
				prop.setProperty("ssl.truststore.password",trustStorePassword);
				prop.setProperty("ssl.truststore.type",trustStoreType);
				prop.setProperty("value.serializer",valuese);
				//prop.setProperty("key.deserializer","org.apache.kafka.common.serialization.StringDeserializer");
				//prop.setProperty("value.deserializer","org.springframework.kafka.support.serializer.JsonDeserializer");
				//prop.setProperty("group.id", "HealthCheck");
				kafka = new KafkaProducer<>(prop);
				logger.debug("Conexao efetuada com o kafka");
			}
			String topico = topicoCadastro;
			logger.debug("produzindo a mensagem para o topico " + topico);
			ProducerRecord<String, Cliente> record = new ProducerRecord(topico,0+"",cliente);
			logger.debug("Mensagem produzida, fazendo o envio da mensagem ao kafka para o topico " + topico);
			RecordMetadata resultado = kafka.send(record).get();
			logger.debug("Mensagem enviada ao kafka para o topico " + topico);
			if (resultado.offset()<0)
			{
				logger.error("Health check falhou ao validar saúde do kafka, não foi possível enviar a mensagem de teste para o tópico  " + topico);
				throw new Exception ("Falha ao enviar mensagem para o topico " + topico);
			}
			topico = topicoDelete;
			logger.debug("produzindo a mensagem para o topico " + topico);
			record = new ProducerRecord(topico,0+"",cliente);
			logger.debug("Mensagem produzida, fazendo o envio da mensagem ao kafka para o topico " + topico);
			resultado = kafka.send(record).get();
			logger.debug("Mensagem enviada ao kafka para o topico " + topico);
			if (resultado.offset()<0)
			{
				logger.error("Health check falhou ao validar saúde do kafka, não foi possível enviar a mensagem de teste para o tópico  " + topico);
				throw new Exception ("Falha ao enviar mensagem para o topico " + topico);
			}
			//kafka.close();
			
			logger.info("Kafka Saudável, Health check completou com suecesso o envio para os dois tópicos");
		}
		catch (Exception e)
		{
			errorCode=1;
			//e.printStackTrace();
			//System.out.println("Kafka não esta saudável: " + e.getMessage());
			logger.error("Falha ao verificar a saude do kafka " + e.getMessage());
			return Health.down().withDetail("Kafka Não saudável", e.getMessage()).build();
			
		}
		return Health.up().build();
	}
	
	
	
}
