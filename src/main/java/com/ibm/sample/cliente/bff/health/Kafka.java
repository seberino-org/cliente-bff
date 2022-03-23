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
	
	//@Value("${spring.kafka.producer.ssl.trust-store-password}")
	//private String trustStorePassword;
	
	//@Value("${spring.kafka.producer.ssl.trust-store-location}")
	//private String trustStoreLocation;
	
	//@Value("${spring.kafka.producer.ssl.trust-store-type}")
	//private String trustStoreType;
	
	//@Value("${spring.kafka.producer.security.protocol}")
	//private String securityProtocol;
	
	//@Value("${spring.kafka.producer.ssl.protocol}")
	//private String sslProtocol;
	
	//@Value("${spring.kafka.properties.sasl.mechanism}")
	//private String saslMechanis;
	
	//@Value("${spring.kafka.properties.sasl.jaas.config}")
	//private String jaasConfig;
	
	KafkaProducer<String, Cliente> kafka;
	
	private Cliente cliente = new Cliente();
	
	@Override
	public Health health() {
		logger.debug("[health] kafka");
		int errorCode = 0;
		try
		{
			logger.debug("Verifying is is already a open connection with Kafka");
			if (kafka ==null)
			{
				logger.debug("setup the kafka connection properties");
				cliente.setCpf(0L);
				cliente.setNome("CLIENTE SINTETICO - HEALTH CHECK");
				cliente.setNumero(0);
				cliente.setNasc(new java.util.Date());
				
				Properties prop = new Properties();
				prop.setProperty("acks","1");
				prop.setProperty("bootstrap.servers",kafkaURL);
				prop.setProperty("key.serializer",keyse);
				//prop.setProperty("sasl.jaas.config",jaasConfig);
				//prop.setProperty("sasl.mechanism",saslMechanis);
				//prop.setProperty("security.protocol",securityProtocol);
				//prop.setProperty("ssl.enabled.protocols",sslProtocol);
				//prop.setProperty("ssl.truststore.location",trustStoreLocation.substring(5));
				//prop.setProperty("ssl.truststore.password",trustStorePassword);
				//prop.setProperty("ssl.truststore.type",trustStoreType);
				prop.setProperty("value.serializer",valuese);
				//prop.setProperty("key.deserializer","org.apache.kafka.common.serialization.StringDeserializer");
				//prop.setProperty("value.deserializer","org.springframework.kafka.support.serializer.JsonDeserializer");
				//prop.setProperty("group.id", "HealthCheck");
				kafka = new KafkaProducer<>(prop);
				logger.debug("Connected to kafka");
			}
			String topico = topicoCadastro;
			logger.debug("producing a message for topic " + topico);
			ProducerRecord<String, Cliente> record = new ProducerRecord(topico,0+"",cliente);
			logger.debug("Message created, sending to the topic " + topico);
			RecordMetadata resultado = kafka.send(record).get();
			logger.debug("Message sent to topic " + topico);
			if (resultado.offset()<0)
			{
				logger.error("Health check failed to validate Kafka health, it wasn't be able to sent a test message for topic " + topico);
				throw new Exception ("Error to send test message for topic " + topico);
			}
			topico = topicoDelete;
			logger.debug("producing a message for topic " + topico);
			record = new ProducerRecord(topico,0+"",cliente);
			logger.debug("Message created, sending to the topic " + topico);
			resultado = kafka.send(record).get();
			logger.debug("Message sent to topic " + topico);
			if (resultado.offset()<0)
			{
				logger.error("Health check failed to validate Kafka health, it wasn't be able to sent a test message for topic " + topico);
				throw new Exception ("Error to send test message for topic " + topico);
			}
			//kafka.close();
			
			logger.info("Kafka Health, Health check sent successfully a test message for both topics");
		}
		catch (Exception e)
		{
			errorCode=1;
			//e.printStackTrace();
			//System.out.println("Kafka não esta saudável: " + e.getMessage());
			logger.error("Health check failed to validate Kafka health " + e.getMessage());
			return Health.down().withDetail("Kafka is not health", e.getMessage()).build();
			
		}
		return Health.up().build();
	}
	
	
	
}
	