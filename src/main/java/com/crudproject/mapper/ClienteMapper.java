package com.crudproject.mapper;

import com.crudproject.dto.cliente.ClienteDTO;
import com.crudproject.dto.cliente.ClienteResponseDTO;
import com.crudproject.model.Cliente;
import com.crudproject.model.Endereco;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Component
public class ClienteMapper {

    private final EnderecoMapper enderecoMapper;

    public ClienteMapper(EnderecoMapper enderecoMapper) {
        this.enderecoMapper = enderecoMapper;
    }

//  Cria uma nova entidade Cliente a partir do DTO.
//  Os endereços embedded no DTO viram entidades Endereco
//  já vinculadas ao Cliente recém-criado (cascade salva tudo junto).
//  NOTA: dataCadastro NÃO é setada aqui — o Service define no salvar.

    public Cliente toEntity(ClienteDTO dto) {
        Cliente cliente = new Cliente();
        copiarCamposBasicos(dto, cliente);

        // Mapeia endereços embedded
        var enderecos = new ArrayList<Endereco>();
        if (dto.getEnderecos() != null) {
            for (var enderecoDto : dto.getEnderecos()) {
                enderecos.add(enderecoMapper.toEntity(enderecoDto, cliente));
            }
        }
        cliente.setEnderecos(enderecos);

        return cliente;
    }

//     Atualiza uma entidade Cliente existente com os campos do DTO.
//     NÃO toca em:
//       - id (imutável, identifica o registro)
//       - tipoPessoa (imutável após cadastro)
//       - dataCadastro (gerada no save original, não muda)
//       - enderecos (sincronizados pelo Service via lógica de sync)

    public void updateEntity(Cliente cliente, ClienteDTO dto) {
        cliente.setNome(dto.getNome());
        cliente.setCpfCnpj(dto.getCpfCnpj());
        cliente.setRgInscricaoEstadual(dto.getRgInscricaoEstadual());
        cliente.setDataNascimento(dto.getDataNascimento());
        cliente.setEmail(dto.getEmail());
        cliente.setAtivo(dto.getAtivo());
    }

//     Converte a entidade em DTO de saída completo,
//     incluindo a lista de endereços já com ids.

    public ClienteResponseDTO toResponse(Cliente cliente) {
        ClienteResponseDTO dto = new ClienteResponseDTO();
        dto.setId(cliente.getId());
        dto.setTipoPessoa(cliente.getTipoPessoa());
        dto.setNome(cliente.getNome());
        dto.setCpfCnpj(cliente.getCpfCnpj());
        dto.setRgInscricaoEstadual(cliente.getRgInscricaoEstadual());
        dto.setDataNascimento(cliente.getDataNascimento());
        dto.setEmail(cliente.getEmail());
        dto.setAtivo(cliente.getAtivo());
        dto.setDataCadastro(cliente.getDataCadastro());

        if (cliente.getEnderecos() != null) {
            dto.setEnderecos(
                    cliente.getEnderecos().stream()
                            .map(enderecoMapper::toResponse)
                            .collect(Collectors.toList())
            );
        }

        return dto;
    }

    // ============================================================
    // Helpers
    // ============================================================

    private void copiarCamposBasicos(ClienteDTO dto, Cliente cliente) {
        cliente.setTipoPessoa(dto.getTipoPessoa());
        cliente.setNome(dto.getNome());
        cliente.setCpfCnpj(dto.getCpfCnpj());
        cliente.setRgInscricaoEstadual(dto.getRgInscricaoEstadual());
        cliente.setDataNascimento(dto.getDataNascimento());
        cliente.setEmail(dto.getEmail());
        cliente.setAtivo(dto.getAtivo());
    }
}
