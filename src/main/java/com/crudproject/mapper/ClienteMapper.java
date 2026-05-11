package com.crudproject.mapper;

import com.crudproject.dto.cliente.ClienteAtualizacaoDTO;
import com.crudproject.dto.cliente.ClienteCadastroDTO;
import com.crudproject.dto.cliente.ClienteResponseDTO;
import com.crudproject.model.Cliente;
import com.crudproject.model.Endereco;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ClienteMapper {

    private final EnderecoMapper enderecoMapper;

    public ClienteMapper(EnderecoMapper enderecoMapper) {
        this.enderecoMapper = enderecoMapper;
    }

    /**
     * Converte DTO de entrada em entidade Cliente, JÁ vinculando
     * os endereços que vieram embedded no DTO.
     *
     * Cada endereço aponta de volta para o Cliente recém-criado
     * (relação bidirecional). A persistência em cascade no @OneToMany
     * cuida de salvar tudo numa transação só.
     */
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

        // Mapeia os endereços embedded, vinculando ao cliente.
        // O mapper de Endereco precisa do Cliente já instanciado pra setar a FK.
        List<Endereco> enderecos = new ArrayList<>();
        if (dto.getEnderecos() != null) {
            for (var enderecoDto : dto.getEnderecos()) {
                enderecos.add(enderecoMapper.toEntity(enderecoDto, cliente));
            }
        }
        cliente.setEnderecos(enderecos);

        return cliente;
    }

    /**
     * Sobrescreve campos de Cliente existente.
     *
     * Recebe ClienteAtualizacaoDTO (não ClienteCadastroDTO) — esse DTO
     * já é "intencionalmente limitado": sem tipoPessoa, sem enderecos.
     * Assim o contrato e o mapper ficam consistentes.
     *
     * Não toca em id, tipoPessoa nem enderecos (esses são imutáveis
     * ou gerenciados em outra rota).
     */
    public void updateEntity(Cliente cliente, ClienteAtualizacaoDTO dto) {
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

        // Endereços
        if (cliente.getEnderecos() != null) {
            dto.setEnderecos(
                    cliente.getEnderecos().stream()
                            .map(enderecoMapper::toResponse)
                            .collect(Collectors.toList())
            );
        }

        return dto;
    }
}
