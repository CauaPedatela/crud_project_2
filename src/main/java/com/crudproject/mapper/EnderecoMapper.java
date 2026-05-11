package com.crudproject.mapper;

import com.crudproject.dto.endereco.EnderecoAtualizacaoDTO;
import com.crudproject.dto.endereco.EnderecoCadastroDTO;
import com.crudproject.dto.endereco.EnderecoResponseDTO;
import com.crudproject.model.Cliente;
import com.crudproject.model.Endereco;
import org.springframework.stereotype.Component;

@Component
public class EnderecoMapper {

    /**
     * Converte o DTO de entrada em entidade Endereco.
     *
     * Recebe o objeto Cliente já buscado pelo Service —
     * o mapper não acessa o banco, só transforma dados.
     */
    public Endereco toEntity(EnderecoCadastroDTO dto, Cliente cliente) {
        Endereco endereco = new Endereco();

        endereco.setCliente(cliente);

        endereco.setLogradouro(dto.getLogradouro());
        endereco.setNumero(dto.getNumero());
        endereco.setCep(dto.getCep());
        endereco.setBairro(dto.getBairro());
        endereco.setTelefone(dto.getTelefone());
        endereco.setCidade(dto.getCidade());
        endereco.setEstado(dto.getEstado());
        endereco.setEnderecoPrincipal(dto.getEnderecoPrincipal());
        endereco.setComplemento(dto.getComplemento());

        return endereco;
    }

    /**
     * Sobrescreve os campos de uma entidade Endereco existente com
     * os dados do DTO de atualização.
     *
     * Recebe EnderecoAtualizacaoDTO (não EnderecoCadastroDTO) —
     * esse DTO não tem clienteId, deixando explícito que a operação
     * não tem como mover o endereço para outro cliente.
     *
     * Usado no fluxo de PUT: o Service busca a entidade pelo id,
     * passa para o mapper aplicar as mudanças, e o id + cliente
     * originais são preservados.
     */
    public void updateEntity(Endereco endereco, EnderecoAtualizacaoDTO dto) {
        endereco.setLogradouro(dto.getLogradouro());
        endereco.setNumero(dto.getNumero());
        endereco.setCep(dto.getCep());
        endereco.setBairro(dto.getBairro());
        endereco.setTelefone(dto.getTelefone());
        endereco.setCidade(dto.getCidade());
        endereco.setEstado(dto.getEstado());
        endereco.setEnderecoPrincipal(dto.getEnderecoPrincipal());
        endereco.setComplemento(dto.getComplemento());
    }

    public EnderecoResponseDTO toResponse(Endereco endereco) {
        EnderecoResponseDTO dto = new EnderecoResponseDTO();

        dto.setId(endereco.getId());
        // Pega só o ID do cliente, evitando expor o objeto Cliente inteiro
        dto.setClienteId(endereco.getCliente() != null ? endereco.getCliente().getId() : null);

        dto.setLogradouro(endereco.getLogradouro());
        dto.setNumero(endereco.getNumero());
        dto.setCep(endereco.getCep());
        dto.setBairro(endereco.getBairro());
        dto.setTelefone(endereco.getTelefone());
        dto.setCidade(endereco.getCidade());
        dto.setEstado(endereco.getEstado());
        dto.setEnderecoPrincipal(endereco.getEnderecoPrincipal());
        dto.setComplemento(endereco.getComplemento());

        return dto;
    }
}
