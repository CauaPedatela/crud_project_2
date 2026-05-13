package com.crudproject.service;

import com.crudproject.dto.cliente.ClienteDTO;
import com.crudproject.dto.cliente.ClienteResponseDTO;
import com.crudproject.mapper.ClienteMapper;
import com.crudproject.model.Cliente;
import com.crudproject.repository.ClienteRepository;
import com.crudproject.service.validation.ClienteValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

// Serviço de Cliente — orquestrador.
// Não tem lógica de validação nem de sincronização de endereços.
// Apenas chama as classes especializadas na ordem correta:
//   ClienteValidator       → validações de negócio
//   ClienteMapper          → conversão DTO ↔ Entity
//   EnderecoSincronizador  → sync da lista de endereços
//   ClienteRepository      → acesso ao banco

@Service
public class ClienteService {

    @Autowired private ClienteRepository clienteRepository;
    @Autowired private ClienteMapper clienteMapper;
    @Autowired private ClienteValidator validator;
    @Autowired private EnderecoSincronizador enderecoSincronizador;

    // ============================================================
    // API PÚBLICA
    // ============================================================

    @Transactional
    public ClienteResponseDTO salvar(ClienteDTO dto) {

        // 1) Valida tudo antes de tocar no banco
        validator.validarCamposObrigatorios(dto);
        validator.validarDocumento(dto.getCpfCnpj(), dto.getTipoPessoa());
        validator.validarUnicidadeDocumento(dto.getCpfCnpj(), null);
        validator.validarEnderecos(dto.getEnderecos());

        // 2) Converte DTO em entidade (com endereços já vinculados ao cliente)
        Cliente cliente = clienteMapper.toEntity(dto);

        // 3) dataCadastro é gerada agora — sempre no momento do salvamento
        cliente.setDataCadastro(LocalDateTime.now());

        // 4) Garante exatamente UM endereço marcado como principal
        enderecoSincronizador.ajustarPrincipal(cliente.getEnderecos());

        // 5) Persiste (cascade salva os endereços junto)
        Cliente salvo = clienteRepository.save(cliente);

        // 6) Converte resultado em DTO de saída
        return clienteMapper.toResponse(salvo);
    }

    public List<ClienteResponseDTO> buscarTodos() {
        return clienteRepository.findAll()
                .stream()
                .map(clienteMapper::toResponse)
                .collect(Collectors.toList());
    }

    public ClienteResponseDTO buscarPorId(Long id) {
        return clienteMapper.toResponse(buscarEntidadePorId(id));
    }

    @Transactional
    public ClienteResponseDTO atualizar(Long id, ClienteDTO dto) {

        // 1) Carrega o cliente existente — lança se não encontrar
        Cliente cliente = buscarEntidadePorId(id);

        // 2) Validações específicas de atualização
        validator.validarTipoPessoaImutavel(cliente, dto);

        // 3) Validações comuns — usa o tipoPessoa da entidade (já validado como imutável)
        validator.validarCamposObrigatorios(dto);
        validator.validarDocumento(dto.getCpfCnpj(), cliente.getTipoPessoa());
        validator.validarUnicidadeDocumento(dto.getCpfCnpj(), id);
        validator.validarEnderecos(dto.getEnderecos());

        // 4) Atualiza campos básicos do cliente (sem tocar em endereços)
        clienteMapper.updateEntity(cliente, dto);

        // 5) Sincroniza endereços (semântica de sync)
        enderecoSincronizador.sincronizar(cliente, dto.getEnderecos());

        // 6) Persiste e retorna
        Cliente salvo = clienteRepository.save(cliente);
        return clienteMapper.toResponse(salvo);
    }

    @Transactional
    public void excluir(Long id) {
        if (!clienteRepository.existsById(id)) {
            throw new RuntimeException("Cliente não encontrado.");
        }
        clienteRepository.deleteById(id);
    }

    // ============================================================
    // HELPER PRIVADO
    // ============================================================

    private Cliente buscarEntidadePorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado."));
    }
}
