package com.crudproject.repository;

import com.crudproject.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// JpaRepository    → métodos básicos: findAll, findById, save, deleteById, count…
// JpaSpecificationExecutor → habilita findAll(Specification), que permite ao
//                            ClienteDAO passar filtros dinâmicos como objetos Java
//                            (sem escrever JPQL/SQL na mão).
// @Repository      → indica ao Spring que essa interface acessa o banco.

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long>,
                                            JpaSpecificationExecutor<Cliente> {
    Optional<Cliente> findByCpfCnpj(String cpfCnpj);

    // Conta apenas os clientes com ativo = ?, sem carregar entidades em memória.
    // Spring Data resolve o nome do método e gera: SELECT count(*) FROM tb_cliente WHERE ativo = ?
    // O método count() (total geral) já vem grátis do JpaRepository.
    long countByAtivo(boolean ativo);
}
