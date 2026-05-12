package com.crudproject.repository;

import com.crudproject.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

//Estendendo JpaRepository, ganhamos os métodos básicos de acesso
//ao banco automaticamente, sem escrever SQL:
//  findAll()        → busca todos os clientes
//  findById(id)     → busca por ID
//  save(cliente)    → salva ou atualiza
//  deleteById(id)   → exclui por ID
//  count()          → conta os registros

// @Repository → indica ao Spring que essa interface
// faz parte da camada de acesso ao banco de dados.

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByCpfCnpj(String cpfCnpj);
}
