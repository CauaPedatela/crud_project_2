package com.crudproject.service;

import com.crudproject.dao.ClienteDAO;
import com.crudproject.dto.cliente.ClienteDTO;
import com.crudproject.dto.cliente.ClienteResponseDTO;
import com.crudproject.mapper.ClienteMapper;
import com.crudproject.model.Cliente;
import com.crudproject.repository.ClienteRepository;
import com.crudproject.service.validation.ClienteValidator;
import com.crudproject.service.validation.DocumentoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

// Serviço de Cliente — orquestrador.
// Não tem lógica de validação nem de sincronização de endereços.
// Apenas chama as classes especializadas na ordem correta:
//   ClienteValidator       → validações de negócio
//   ClienteMapper          → conversão DTO ↔ Entity
//   EnderecoSincronizador  → sync da lista de endereços
//   ClienteRepository      → acesso direto ao banco (operações padrão JPA)
//   ClienteDAO             → acesso ao banco (busca com filtros dinâmicos)

@Service
public class ClienteService {

    // Operações padrão (save, findAll, findById, deleteById) → direto no Repository
    @Autowired private ClienteRepository clienteRepository;

    // Busca com filtros dinâmicos → via DAO (usa Specification API)
    @Autowired private ClienteDAO clienteDAO;

    @Autowired private ClienteMapper clienteMapper;
    @Autowired private ClienteValidator validator;
    @Autowired private EnderecoSincronizador enderecoSincronizador;

    // ============================================================
    // API PÚBLICA
    // ============================================================

    @Transactional
    public ClienteResponseDTO salvar(ClienteDTO dto) {
        // 1. A MÁGICA ACONTECE AQUI: Limpa os dados antes de qualquer coisa
        normalizarDados(dto);

        // 2. Agora o validator recebe os dados já limpos!
        validator.validarCamposObrigatorios(dto);
        validator.validarDocumento(dto.getCpfCnpj(), dto.getTipoPessoa());
        validator.validarUnicidadeDocumento(dto.getCpfCnpj(), null);
        validator.validarEnderecos(dto.getEnderecos());

        Cliente cliente = clienteMapper.toEntity(dto);
        cliente.setDataCadastro(LocalDateTime.now());
        enderecoSincronizador.ajustarPrincipal(cliente.getEnderecos());

        Cliente salvo = clienteRepository.save(cliente);
        return clienteMapper.toResponse(salvo);
    }

    @Transactional
    public List<ClienteResponseDTO> buscarTodos() {
        return clienteRepository.findAll()
                .stream()
                .map(clienteMapper::toResponse)
                .collect(Collectors.toList());
    }

    // Recebe os filtros como Strings (vindos da página Wicket),
    // converte as datas para LocalDate e delega a query ao DAO.
    @Transactional
    public List<ClienteResponseDTO> buscarComFiltros(
            String termo,
            String filtroAtivo,
            String filtroTipo,
            String dataInicio,
            String dataFim) {

        LocalDate inicio = (dataInicio != null && !dataInicio.isBlank())
                ? LocalDate.parse(dataInicio) : null;
        LocalDate fim = (dataFim != null && !dataFim.isBlank())
                ? LocalDate.parse(dataFim) : null;

        return clienteDAO.buscarComFiltros(termo, filtroAtivo, filtroTipo, inicio, fim)
                .stream()
                .map(clienteMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ClienteResponseDTO buscarPorId(Long id) {
        return clienteMapper.toResponse(buscarEntidadePorId(id));
    }

    @Transactional
    public ClienteResponseDTO atualizar(Long id, ClienteDTO dto) {
        Cliente cliente = buscarEntidadePorId(id);

        // 1. Limpa os dados antes de validar
        normalizarDados(dto);

        validator.validarTipoPessoaImutavel(cliente, dto);
        validator.validarCamposObrigatorios(dto);
        validator.validarDocumento(dto.getCpfCnpj(), cliente.getTipoPessoa());
        validator.validarUnicidadeDocumento(dto.getCpfCnpj(), id);
        validator.validarEnderecos(dto.getEnderecos());

        clienteMapper.updateEntity(cliente, dto);
        enderecoSincronizador.sincronizar(cliente, dto.getEnderecos());

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

    private void normalizarDados(ClienteDTO dto) {
        dto.setCpfCnpj(DocumentoUtil.limparFormatacao(dto.getCpfCnpj()));
        if (dto.getRgInscricaoEstadual() != null && !dto.getRgInscricaoEstadual().isBlank()) {
            dto.setRgInscricaoEstadual(dto.getRgInscricaoEstadual().trim().toUpperCase());
        }

        if (dto.getEnderecos() != null) {
            dto.getEnderecos().forEach(e -> {
                e.setCep(DocumentoUtil.limparFormatacao(e.getCep()));
                e.setTelefone(DocumentoUtil.limparFormatacao(e.getTelefone()));
            });
        }
    }
}
