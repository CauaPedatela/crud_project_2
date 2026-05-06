package com.crudproject.repository;

import com.crudproject.model.Endereco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository de Endereço.
 *
 * Estendendo JpaRepository, ganhamos os métodos básicos de acesso
 * ao banco automaticamente, sem escrever SQL:
 *   findAll()        → busca todos os endereços
 *   findById(id)     → busca por ID
 *   save(endereco)   → salva ou atualiza
 *   deleteById(id)   → exclui por ID
 *
 * @Repository → indica ao Spring que essa interface
 * faz parte da camada de acesso ao banco de dados.
 */
@Repository
public interface EnderecoRepository extends JpaRepository<Endereco, Long> {

    // Os métodos básicos já existem — herdados do JpaRepository.
    // Métodos de busca específicos serão adicionados aqui futuramente.

    /**
     * Busca todos os endereços de um cliente específico.
     * O Spring gera o SQL automaticamente pelo nome do método:
     * SELECT * FROM tb_endereco WHERE cliente_id = ?
     *
     * Usado para listar os endereços de um cliente na tela.
     */
    List<Endereco> findByClienteId(Long clienteId);

}
