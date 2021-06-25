package com.ibm.sample.cliente.bff;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ibm.sample.cliente.bff.dto.Cliente;
import com.ibm.sample.cliente.bff.dto.RespostaBFF;

@RestController
public class ClienteBFFRest {

	
	@PostMapping("/bff/cliente")
	public RespostaBFF processaCadastro(Cliente cliente)
	{
		RespostaBFF resposta = new RespostaBFF();
		
		resposta.setCodigo("200-SUCESSO");
		resposta.setMensagem("Cadastro efetuado com sucesso!");
		return resposta;
	}
}
