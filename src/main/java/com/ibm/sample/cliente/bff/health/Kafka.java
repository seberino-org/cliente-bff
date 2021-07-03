package com.ibm.sample.cliente.bff.health;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import com.ibm.sample.cliente.bff.dto.Cliente;

@Component
public class Kafka implements HealthIndicator {

	
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
		int errorCode = 0;
		try
		{
			if (kafka ==null)
			{
				
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
			}
			String topico = topicoCadastro;
			ProducerRecord<String, Cliente> record = new ProducerRecord(topico,0+"",cliente);
			RecordMetadata resultado = kafka.send(record).get();
			if (resultado.offset()<0)
			{
				throw new Exception ("Falha ao enviar mensagem para o topico " + topico);
			}
			topico = topicoDelete;
			record = new ProducerRecord(topico,0+"",cliente);
			resultado = kafka.send(record).get();
			if (resultado.offset()<0)
			{
				throw new Exception ("Falha ao enviar mensagem para o topico " + topico);
			}
			//kafka.close();
			
	
		}
		catch (Exception e)
		{
			errorCode=1;
			e.printStackTrace();
			//System.out.println("Kafka não esta saudável: " + e.getMessage());
			return Health.down().withDetail("Kafka Não saudável", e.getMessage()).build();
			
		}
		return Health.up().build();
	}
	
	
	
}
