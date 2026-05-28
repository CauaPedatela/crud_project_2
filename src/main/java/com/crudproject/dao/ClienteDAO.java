package com.crudproject.dao;

import com.crudproject.model.Cliente;
import com.crudproject.model.TipoPessoa;
import com.crudproject.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// DAO de Cliente — responsável pelas buscas que o JpaRepository padrão
// não consegue fazer sozinho: queries com filtros dinâmicos (WHERE variável).
// As operações simples (save, findById, deleteById, findAll) continuam
// sendo chamadas diretamente pelo Service via ClienteRepository.

@Repository
public class ClienteDAO {

    @Autowired
    private ClienteRepository clienteRepository;

    // ============================================================
    // BUSCA FILTRADA — usa JPA Specification API
    // ============================================================
    //
    // Specification<T> é um predicado — equivale a um trecho do WHERE no SQL.
    // Specification.where(null) = sem restrição (retornaria tudo).
    // .and(outraSpec)           = encadeia com AND.
    // clienteRepository.findAll(spec) manda a query completa para o banco;
    // o Hibernate gera o SQL e retorna apenas os registros que passam em
    // todos os filtros. Nada é carregado em memória desnecessariamente.

    // Lista completa (sem paginação) — usada por relatórios e contadores do Wicket.
    // Internamente delega a montagem das Specifications ao helper privado.
    public List<Cliente> buscarComFiltros(
            String termo,
            String filtroAtivo,
            String filtroTipo,
            LocalDate dataInicio,
            LocalDate dataFim) {

        // Ordena por dataCadastro DESC para que clientes recém-criados apareçam no topo da listagem.
        return clienteRepository.findAll(
                montarSpecification(termo, filtroAtivo, filtroTipo, dataInicio, dataFim),
                Sort.by(Sort.Direction.DESC, "dataCadastro"));
    }

    // Busca paginada — usada pelo controller REST (Angular) e pelo Wicket (DataProvider).
    // O Pageable carrega: número da página, tamanho da página e Sort.
    // Spring Data gera AUTOMATICAMENTE duas queries:
    //   1) SELECT count(*) FROM tb_cliente WHERE <filtros>           (para totalElements)
    //   2) SELECT * FROM tb_cliente WHERE <filtros> LIMIT ? OFFSET ?  (para a página atual)
    public Page<Cliente> buscarComFiltrosPaginado(
            String termo,
            String filtroAtivo,
            String filtroTipo,
            LocalDate dataInicio,
            LocalDate dataFim,
            Pageable pageable) {

        return clienteRepository.findAll(
                montarSpecification(termo, filtroAtivo, filtroTipo, dataInicio, dataFim),
                pageable);
    }

    // Apenas conta os registros que passam nos filtros — sem buscar nenhum dado.
    // Usado pelo IDataProvider.size() do Wicket para saber o total sem carregar a página.
    // SQL gerado: SELECT count(*) FROM tb_cliente WHERE <filtros>
    public long contarComFiltros(
            String termo,
            String filtroAtivo,
            String filtroTipo,
            LocalDate dataInicio,
            LocalDate dataFim) {

        return clienteRepository.count(
                montarSpecification(termo, filtroAtivo, filtroTipo, dataInicio, dataFim));
    }

    // ============================================================
    // HELPER PRIVADO — monta a Specification combinando os filtros.
    // Reutilizado pelos 3 métodos públicos acima (busca, busca paginada, count).
    // ============================================================
    private Specification<Cliente> montarSpecification(
            String termo,
            String filtroAtivo,
            String filtroTipo,
            LocalDate dataInicio,
            LocalDate dataFim) {

        Specification<Cliente> spec = Specification.where(null);

        if (termo != null && !termo.isBlank()) {
            spec = spec.and(comTermo(termo));
        }
        if ("ativo".equals(filtroAtivo)) {
            spec = spec.and(comAtivo(true));
        } else if ("inativo".equals(filtroAtivo)) {
            spec = spec.and(comAtivo(false));
        }
        if ("PF".equals(filtroTipo)) {
            spec = spec.and(comTipo(TipoPessoa.FISICA));
        } else if ("PJ".equals(filtroTipo)) {
            spec = spec.and(comTipo(TipoPessoa.JURIDICA));
        }
        if (dataInicio != null) {
            spec = spec.and(cadastradoDepoisDe(dataInicio));
        }
        if (dataFim != null) {
            spec = spec.and(cadastradoAntesDe(dataFim));
        }

        return spec;
    }

    // ============================================================
    // CONSTRUTORES DE PREDICADO (Specification helpers)
    // Cada método abaixo vira um trecho do WHERE no SQL gerado.
    // ============================================================

    // WHERE LOWER(nome) LIKE '%termo%'
    //    OR cpf_cnpj    LIKE '%termo%'
    //    OR LOWER(email) LIKE '%termo%'
    private Specification<Cliente> comTermo(String termo) {
        return (root, query, cb) -> {
            String like = "%" + termo.toLowerCase() + "%";
            return cb.or(
                cb.like(cb.lower(root.get("nome")),  like),
                cb.like(root.get("cpfCnpj"),         "%" + termo + "%"),
                cb.like(cb.lower(root.get("email")), like)
            );
        };
    }

    // WHERE ativo = true  (ou false)
    private Specification<Cliente> comAtivo(boolean ativo) {
        return (root, query, cb) -> cb.equal(root.get("ativo"), ativo);
    }

    // WHERE tipo_pessoa = 'FISICA'  (ou 'JURIDICA')
    private Specification<Cliente> comTipo(TipoPessoa tipo) {
        return (root, query, cb) -> cb.equal(root.get("tipoPessoa"), tipo);
    }

    // WHERE data_cadastro >= '2024-01-01 00:00:00'
    private Specification<Cliente> cadastradoDepoisDe(LocalDate data) {
        return (root, query, cb) ->
            cb.greaterThanOrEqualTo(
                root.<LocalDateTime>get("dataCadastro"),
                data.atStartOfDay()
            );
    }

    // WHERE data_cadastro <= '2024-01-31 23:59:59'
    private Specification<Cliente> cadastradoAntesDe(LocalDate data) {
        return (root, query, cb) ->
            cb.lessThanOrEqualTo(
                root.<LocalDateTime>get("dataCadastro"),
                data.atTime(23, 59, 59)
            );
    }
}
