package com.ibm.sample.cliente.bff;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.ibm.sample.cliente.bff.dto.Cliente;
import com.ibm.sample.cliente.bff.dto.RespostaBFF;
import com.ibm.sample.cliente.bff.dto.RetornoCliente;

@RestController
public class ClienteBFFRest {

	@Value("${cliente-rest.url}")
	private String urlClienteRest; 
	
	Logger logger = LoggerFactory.getLogger(ClienteBFFRest.class);
	
	@Value("${delete-cliente-kafka-topico}")
	private String deleteTopic; 

	@Value("${cliente-kafka-topico}")
	private String cadastroTopic; 
	
	@Autowired
	private KafkaTemplate<String, Cliente> kafka;
	
	@Autowired
	private RestTemplate clienteRest;
	
	@CrossOrigin(origins = "*")
	@GetMapping("/bff/cliente/pesquisa/{nome}")
	public List<Cliente> pesquisaClientes(@PathVariable String nome)
	{	
		logger.debug("[pesquisaClientes] " + nome);
		logger.info("vai peesquisar clientes com o nome contendo: " + nome);
		List<Cliente> resultado = clienteRest.getForObject(urlClienteRest+"/pesquisa/" + nome, List.class);
		if (resultado!=null)
		{
			logger.debug("Encontrado: " + resultado.size() + " clientes na pesuisa");
		}

		return resultado;
	}
	
	
	@GetMapping("/bff/cliente/{cpf}")
	public ResponseEntity<RetornoCliente> recuperaCliente(@PathVariable Long cpf)
	{
		logger.debug("[recuperaCliente] " + cpf);
		try
		{
			logger.debug("Vai pesquisar o cliente pelo cpf " + cpf);
		    RetornoCliente retorno = clienteRest.getForObject(urlClienteRest+"/" + cpf, RetornoCliente.class);
			if (retorno!=null && logger.isDebugEnabled())
			{
				logger.debug("resultado da busca: " + retorno.getMensagem());
				if (retorno.getCliente()!=null)
				{
					logger.debug("Cliente: " + retorno.getCliente().getNome());
				}
			}
		    return ResponseEntity.ok(retorno);
		}
		catch (Exception e)
		{
			logger.warn("Falha na pesquisa de cliente pelo CPF " + e.getMessage());
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		
	}
	
	@CrossOrigin(origins = "*")
	@DeleteMapping("/bff/cliente/{cpf}")
	public ResponseEntity<RespostaBFF> excluiCliente(@PathVariable Long cpf)
	{
		logger.debug("[excluiCliente] " + cpf);
		RespostaBFF resposta = new RespostaBFF();
		
		try
		{
			logger.debug("vai pesquisar se o cliente existe!");			
			RetornoCliente retorno = clienteRest.getForObject(urlClienteRest+"/" + cpf, RetornoCliente.class);
			logger.debug(retorno.getMensagem());
			logger.debug("Enviando mensagem para o topico Kafka para realizar a exclusao de forma asyncrona");
			enviaMensagemKafka(this.deleteTopic, retorno.getCliente());
			logger.debug("Mensagem enviada para o kafka");
			resposta.setCodigo("202-EXCLUIDO");
			resposta.setMensagem("Deleção submetida com sucesso! " );
			logger.info(resposta.getCodigo() + " - " + resposta.getMensagem());
			return ResponseEntity.ok(resposta);
		}
		catch (Exception e)
		{
			logger.warn("Problemas durante a exclusão do cliente: "  + e.getMessage());
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		
	}
	
	@CrossOrigin(origins = "*")
	@PostMapping("/bff/cliente")
	public ResponseEntity<RespostaBFF> processaCadastro(@RequestBody Cliente cliente)
	{
		logger.debug("[processaCadastro] " + cliente.getNome());
		RespostaBFF resposta = new RespostaBFF();
		
		try
		{
			logger.debug("Validando se os dados informados para cadastro estão corretos");
			this.validaCliente(cliente);
			logger.debug("Dados validados com sucesso, verificando se o cliente já existe na base de dados");
			if (this.clienteExiste(cliente.getCpf()))
			{
				logger.info("CLiente já existe na base de dados, cadastro abortado para evitar duplicidade. CLiente CPF: " + cliete.getCpf());
				return new ResponseEntity<>(HttpStatus.ALREADY_REPORTED);
			} 
			logger.debug("Vai enviar a mensagem para o topico Kafka para processamento do cadastro de forma assíncrona");
			enviaMensagemKafka(this.cadastroTopic, cliente);
			logger.debug("Mensagem enviada com sucesso ao topico kafka");
		
			resposta.setCodigo("200-SUCESSO");
			resposta.setMensagem("Cadastro submetido com sucesso! " );
			logger.info(resposta.getCodigo() + " - " + resposta.getMensagem());
			return ResponseEntity.ok(resposta);
		}
		catch (Exception e)
		{
			logger.error("Falha durante o cadastro do cliente: "  + e.getMessage());
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		
	}
	private void enviaMensagemKafka(String topico, Cliente cliente)
	{
		kafka.send(topico,cliente);
	}
	
	private boolean clienteExiste(Long cpf)
	{
		try
		{
			RetornoCliente resultado = clienteRest.getForObject(urlClienteRest+"/" + cpf, RetornoCliente.class);
			if (resultado.getCodigo().equals("200-FOUND"))
			{
				return true;
			}
		}
		catch (Exception e)
		{
			
		}
		return false;
	}
	
	private void validaCliente(Cliente cliente) throws Exception
	{
		if (cliente==null)
		{
			throw new Exception("Payload inváido, não foram encontrados os dados do cliente");
		}
		if (cliente.getCpf()==null || cliente.getCpf()==0)
		{
			throw new Exception("CPF é um campo obrigatório");
		}
		if (cliente.getNome()==null || cliente.getNome().length()==0)
		{
			throw new Exception("Nome é um campo obrigatório");
		}
		
	}
	
}
