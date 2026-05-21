package com.crudproject.mapper;

import com.crudproject.dto.endereco.EnderecoDTO;
import com.crudproject.dto.endereco.EnderecoResponseDTO;
import com.crudproject.model.Cliente;
import com.crudproject.model.Endereco;
import org.springframework.stereotype.Component;

@Component
public class EnderecoMapper {

//     Cria uma nova entidade Endereco a partir do DTO,
//     vinculando ao Cliente passado pelo Service.

    public Endereco toEntity(EnderecoDTO dto, Cliente cliente) {
        Endereco endereco = new Endereco();
        endereco.setCliente(cliente);
        copiarCamposBasicos(dto, endereco);
        return endereco;
    }

//     Atualiza uma entidade Endereco existente com os campos do DTO.
//     Mantém o id e o cliente — só sobrescreve os dados.

    public void updateEntity(Endereco endereco, EnderecoDTO dto) {
        copiarCamposBasicos(dto, endereco);
    }

//     Converte a entidade em DTO de saída.

    public EnderecoResponseDTO toResponse(Endereco endereco) {
        EnderecoResponseDTO dto = new EnderecoResponseDTO();
        dto.setId(endereco.getId());
        dto.setTipo(endereco.getTipo());
        dto.setLogradouro(endereco.getLogradouro());
        dto.setNumero(endereco.getNumero());
        dto.setComplemento(endereco.getComplemento());
        dto.setBairro(endereco.getBairro());
        dto.setCidade(endereco.getCidade());
        dto.setEstado(endereco.getEstado());
        dto.setCep(endereco.getCep());
        dto.setPais(endereco.getPais());
        dto.setTelefone(endereco.getTelefone());
        dto.setPrincipal(endereco.getPrincipal());
        return dto;
    }

    // ============================================================
    // Helpers
    // ============================================================

    private void copiarCamposBasicos(EnderecoDTO dto, Endereco endereco) {
        endereco.setTipo(dto.getTipo());
        endereco.setLogradouro(dto.getLogradouro());
        endereco.setNumero(dto.getNumero());
        endereco.setComplemento(dto.getComplemento());
        endereco.setBairro(dto.getBairro());
        endereco.setCidade(dto.getCidade());
        endereco.setEstado(dto.getEstado());
        endereco.setCep(dto.getCep());
        endereco.setPais(dto.getPais());
        endereco.setTelefone(dto.getTelefone());
        endereco.setPrincipal(dto.getPrincipal());
    }
}
