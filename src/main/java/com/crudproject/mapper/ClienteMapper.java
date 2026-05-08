package com.crudproject.mapper;

import com.crudproject.dto.cliente.ClienteCadastroDTO;
import com.crudproject.dto.cliente.ClienteResponseDTO;
import com.crudproject.model.Cliente;
import org.springframework.stereotype.Component;

@Component
public class ClienteMapper {

    public Cliente toEntity(ClienteCadastroDTO dto) {
        Cliente cliente = new Cliente();

        cliente.setTipoPessoa(dto.getTipoPessoa());

        // Campos PF
        cliente.setCpf(dto.getCpf());
        cliente.setNome(dto.getNome());
        cliente.setRg(dto.getRg());
        cliente.setDataNascimento(dto.getDataNascimento());

        // Campos PJ
        cliente.setCnpj(dto.getCnpj());
        cliente.setRazaoSocial(dto.getRazaoSocial());
        cliente.setInscricaoEstadual(dto.getInscricaoEstadual());
        cliente.setDataCriacao(dto.getDataCriacao());

        // Comuns
        cliente.setEmail(dto.getEmail());
        cliente.setAtivo(dto.getAtivo());

        return cliente;
    }


    public ClienteResponseDTO toResponse(Cliente cliente) {
        ClienteResponseDTO dto = new ClienteResponseDTO();

        dto.setId(cliente.getId());
        dto.setTipoPessoa(cliente.getTipoPessoa());

        // Campos PF
        dto.setCpf(cliente.getCpf());
        dto.setNome(cliente.getNome());
        dto.setRg(cliente.getRg());
        dto.setDataNascimento(cliente.getDataNascimento());

        // Campos PJ
        dto.setCnpj(cliente.getCnpj());
        dto.setRazaoSocial(cliente.getRazaoSocial());
        dto.setInscricaoEstadual(cliente.getInscricaoEstadual());
        dto.setDataCriacao(cliente.getDataCriacao());

        // Comuns
        dto.setEmail(cliente.getEmail());
        dto.setAtivo(cliente.getAtivo());

        return dto;
    }
}
