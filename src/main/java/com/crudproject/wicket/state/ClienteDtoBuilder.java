/*
 * ClienteDtoBuilder — utilitários de conversão usados pelos panels Wicket
 * para montar um ClienteDTO a partir do estado atual do cliente (lido do
 * service) e converter listas EnderecoResponseDTO ↔ EnderecoDTO.
 *
 * Centraliza a lógica duplicada que existia em ListagemClientesPage e
 * DetalhesClientePage. Ao migrar para Angular, este utilitário some — o
 * frontend trabalha direto com DTOs JSON sem precisar dessa ponte.
 */
package com.crudproject.wicket.state;

import com.crudproject.dto.cliente.ClienteDTO;
import com.crudproject.dto.cliente.ClienteResponseDTO;
import com.crudproject.dto.endereco.EnderecoDTO;
import com.crudproject.dto.endereco.EnderecoResponseDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class ClienteDtoBuilder {

    private ClienteDtoBuilder() { }

    // Converte uma lista de EnderecoResponseDTO em uma lista de EnderecoDTO,
    // preservando os ids para que o service entenda como UPDATE (e não INSERT).
    public static List<EnderecoDTO> toEnderecosDTOs(List<EnderecoResponseDTO> lista) {
        if (lista == null) return new ArrayList<>();
        return lista.stream().map(e -> {
            EnderecoDTO dto = new EnderecoDTO();
            dto.setId(e.getId());
            dto.setTipo(e.getTipo());
            dto.setLogradouro(e.getLogradouro());
            dto.setNumero(e.getNumero());
            dto.setComplemento(e.getComplemento());
            dto.setBairro(e.getBairro());
            dto.setCidade(e.getCidade());
            dto.setEstado(e.getEstado());
            dto.setCep(e.getCep());
            dto.setPais(e.getPais());
            dto.setTelefone(e.getTelefone());
            dto.setPrincipal(e.getPrincipal());
            return dto;
        }).collect(Collectors.toList());
    }

    // Monta um ClienteDTO completo a partir de um ClienteResponseDTO existente,
    // substituindo apenas a lista de endereços. Os panels de CRUD de endereço
    // usam este helper para evitar duplicar a construção do DTO.
    public static ClienteDTO comEnderecos(ClienteResponseDTO atual, List<EnderecoDTO> enderecos) {
        ClienteDTO dto = new ClienteDTO();
        dto.setTipoPessoa(atual.getTipoPessoa());
        dto.setNome(atual.getNome());
        dto.setCpfCnpj(atual.getCpfCnpj());
        dto.setDataNascimento(atual.getDataNascimento());
        dto.setEmail(atual.getEmail());
        dto.setAtivo(atual.getAtivo());
        dto.setRgInscricaoEstadual(atual.getRgInscricaoEstadual());
        dto.setEnderecos(enderecos);
        return dto;
    }
}
