package com.ibm.sample.cliente.bff;

import java.util.List;

import javax.websocket.server.PathParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
		return clienteRest.getForObject(urlClienteRest+"/pesquisa/" + nome, List.class);
	}
	
	
	@GetMapping("/bff/cliente/{cpf}")
	public RetornoCliente recuperaCliente(@PathVariable Long cpf)
	{
		return clienteRest.getForObject(urlClienteRest+"/" + cpf, RetornoCliente.class);
		
	}
	
	@CrossOrigin(origins = "*")
	@DeleteMapping("/bff/cliente")
	public RespostaBFF excluiCliente(@RequestBody Cliente cliente)
	{
		RespostaBFF resposta = new RespostaBFF();
		
		try
		{
			this.validaCliente(cliente);
			if (!this.clienteExiste(cliente.getCpf()))
			{
				resposta.setCodigo("404-CLIENTE NAO CADASTRADO");
				resposta.setMensagem("Cliente nao encontrado com esse CPF!" );
				return resposta;
			}
			
			enviaMensagemKafka(this.deleteTopic, cliente);
		
			resposta.setCodigo("202-EXCLUIDO");
			resposta.setMensagem("Deleção submetida com sucesso! " );
			return resposta;
		}
		catch (Exception e)
		{
			resposta.setCodigo("400-BAD REQUEST");
			resposta.setMensagem("Erro no processamento: " + e.getMessage());
			e.printStackTrace();
			return resposta;
		}
		
	}
	
	@CrossOrigin(origins = "*")
	@PostMapping("/bff/cliente")
	public RespostaBFF processaCadastro(@RequestBody Cliente cliente)
	{
		RespostaBFF resposta = new RespostaBFF();
		
		try
		{
			this.validaCliente(cliente);
			if (this.clienteExiste(cliente.getCpf()))
			{
				resposta.setCodigo("201-CLIENTE JA CADASTRADO");
				resposta.setMensagem("Cliente ja cadastrado anteriormente com esse CPF!" );
				return resposta;
			}
			
			enviaMensagemKafka(this.cadastroTopic, cliente);
		
			resposta.setCodigo("200-SUCESSO");
			resposta.setMensagem("Cadastro submetido com sucesso! " );
			return resposta;
		}
		catch (Exception e)
		{
			resposta.setCodigo("400-BAD REQUEST");
			resposta.setMensagem("Erro no processamento: " + e.getMessage());
			e.printStackTrace();
			return resposta;
		}
		
	}
	private void enviaMensagemKafka(String topico, Cliente cliente)
	{
		kafka.send(topico,cliente);
	}
	
	private boolean clienteExiste(Long cpf)
	{
		RetornoCliente resultado = clienteRest.getForObject(urlClienteRest+"/" + cpf, RetornoCliente.class);
		if (resultado.getCodigo().equals("200-FOUND"))
		{
			return true;
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
