package com.crudproject.repository;

import com.crudproject.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository de Cliente.
 *
 * Estendendo JpaRepository, ganhamos os métodos básicos de acesso
 * ao banco automaticamente, sem escrever SQL:
 *   findAll()        → busca todos os clientes
 *   findById(id)     → busca por ID
 *   save(cliente)    → salva ou atualiza
 *   deleteById(id)   → exclui por ID
 *   count()          → conta os registros
 * @Repository → indica ao Spring que essa interface
 * faz parte da camada de acesso ao banco de dados.
 */
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    // Os métodos básicos já existem — herdados do JpaRepository.
    // Métodos de busca específicos serão adicionados aqui futuramente.

    /**
     * Lá no service usaremos esse método "findByCpf" pra verificar se já existe cliente
     * cadastrado com o cpf(ou cnpj) que é passado como parâmetro através de um
     * Usado para verificar se um CPF ou CNPJ já está cadastrado.
     */
    Optional<Cliente> findByCpf(String cpf);
    Optional<Cliente> findByCnpj(String cnpj);

}
