package com.crudproject.service;

import com.crudproject.dao.ClienteDAO;
import com.crudproject.dto.ContadoresDTO;
import com.crudproject.dto.PageResponseDTO;
import com.crudproject.dto.cliente.ClienteDTO;
import com.crudproject.dto.cliente.ClienteResponseDTO;
import com.crudproject.mapper.ClienteMapper;
import com.crudproject.model.Cliente;
import com.crudproject.repository.ClienteRepository;
import com.crudproject.service.validation.ClienteValidator;
import com.crudproject.service.validation.DocumentoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    // Recebe os filtros como Strings (vindos da página Wicket),
    // converte as datas para LocalDate e delega a query ao DAO.
    // Retorna a lista COMPLETA — usada pelos relatórios (PDF/Excel) e pelo endpoint /api/clientes.
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

    // Busca PAGINADA — usada pelo Angular (via /api/clientes/buscar) e pelo Wicket (DataProvider).
    // O Pageable carrega page, size e Sort (montado pelo Spring a partir dos query params,
    // ou pelo Wicket via PageRequest.of(...)).
    // Vantagem sobre buscarComFiltros: apenas a página atual trafega entre banco e servidor.
    @Transactional
    public PageResponseDTO<ClienteResponseDTO> buscarComFiltrosPaginado(
            String termo,
            String filtroAtivo,
            String filtroTipo,
            String dataInicio,
            String dataFim,
            Pageable pageable) {

        LocalDate inicio = (dataInicio != null && !dataInicio.isBlank())
                ? LocalDate.parse(dataInicio) : null;
        LocalDate fim = (dataFim != null && !dataFim.isBlank())
                ? LocalDate.parse(dataFim) : null;

        Page<Cliente> pagina = clienteDAO.buscarComFiltrosPaginado(
                termo, filtroAtivo, filtroTipo, inicio, fim, pageable);

        // Page.map() converte cada Cliente em ClienteResponseDTO preservando a metadata
        // (totalElements, totalPages, etc) — depois empacotamos no nosso DTO próprio.
        return PageResponseDTO.from(pagina.map(clienteMapper::toResponse));
    }

    // Apenas conta os registros que passam nos filtros — sem trazer nenhum dado.
    // Usado pelo IDataProvider.size() do Wicket para descobrir quantas páginas existem.
    @Transactional
    public long contarComFiltros(
            String termo,
            String filtroAtivo,
            String filtroTipo,
            String dataInicio,
            String dataFim) {

        LocalDate inicio = (dataInicio != null && !dataInicio.isBlank())
                ? LocalDate.parse(dataInicio) : null;
        LocalDate fim = (dataFim != null && !dataFim.isBlank())
                ? LocalDate.parse(dataFim) : null;

        return clienteDAO.contarComFiltros(termo, filtroAtivo, filtroTipo, inicio, fim);
    }

    // Agregados para o header da listagem (total e ativos).
    // Usa apenas COUNT no banco — nenhuma entidade é carregada em memória.
    @Transactional
    public ContadoresDTO contadores() {
        long total = clienteRepository.count();                  // SELECT count(*) FROM tb_cliente
        long ativos = clienteRepository.countByAtivo(true);      // SELECT count(*) FROM tb_cliente WHERE ativo = true
        return new ContadoresDTO(total, ativos);
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
        // Impede que o principal seja desmarcado sem que outro tenha sido promovido.
        // Deve vir ANTES do sincronizar(), que silenciosamente corrigiria o problema.
        validator.validarPrincipalMantido(dto.getEnderecos());

        clienteMapper.updateEntity(cliente, dto);
        enderecoSincronizador.sincronizar(cliente, dto.getEnderecos());

        Cliente salvo = clienteRepository.save(cliente);
        return clienteMapper.toResponse(salvo);
    }

    // Roda todo o pipeline de normalização + validação sem persistir nada.
    // Usado pelo ClienteImportacaoService no passo 1 (checar todas as linhas antes de salvar qualquer uma).
    @Transactional
    public void validarParaImportacao(ClienteDTO dto) {
        normalizarDados(dto);
        validator.validarCamposObrigatorios(dto);
        validator.validarDocumento(dto.getCpfCnpj(), dto.getTipoPessoa());
        validator.validarUnicidadeDocumento(dto.getCpfCnpj(), null);
        validator.validarEnderecos(dto.getEnderecos());
        // sem save — apenas valida
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
