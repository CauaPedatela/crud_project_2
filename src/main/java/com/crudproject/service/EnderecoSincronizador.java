package com.crudproject.service;

import com.crudproject.dto.endereco.EnderecoDTO;
import com.crudproject.mapper.EnderecoMapper;
import com.crudproject.model.Cliente;
import com.crudproject.model.Endereco;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Responsável pela lógica de sincronização da lista de endereços
 * de um cliente.
 *
 * Duas operações públicas:
 *   - sincronizar()        → usada no PUT (merge inteligente)
 *   - ajustarPrincipal()   → usada no POST e no final do sincronizar()
 */

@Component
public class EnderecoSincronizador {

    @Autowired
    private EnderecoMapper enderecoMapper;

    /**
     * Sincroniza a lista atual de endereços do cliente com a lista
     * recebida no DTO. Três cenários por endereço:
     *
     *   DTO tem id existente → atualiza o endereço correspondente
     *   DTO sem id           → cria novo endereço
     *   Existe no banco mas
     *   não está no DTO      → deleta (orphanRemoval = true cuida)
     *
     * Ao final, garante que exatamente UM endereço seja principal.
     */
    public void sincronizar(Cliente cliente, List<EnderecoDTO> enderecosDoDto) {

        // Mapa dos endereços atuais por id, para lookup rápido
        Map<Long, Endereco> atuaisPorId = cliente.getEnderecos().stream()
                .filter(e -> e.getId() != null)
                .collect(Collectors.toMap(Endereco::getId, e -> e));

        // Lista dos endereços que vão FICAR (atualizados ou criados)
        List<Endereco> resultantes = new ArrayList<>();

        for (EnderecoDTO enderecoDto : enderecosDoDto) {
            if (enderecoDto.getId() != null && atuaisPorId.containsKey(enderecoDto.getId())) {
                // ATUALIZAR — endereço já existe
                Endereco existente = atuaisPorId.get(enderecoDto.getId());
                enderecoMapper.updateEntity(existente, enderecoDto);
                resultantes.add(existente);
            } else {
                // CRIAR — endereço novo (id null ou id inexistente)
                Endereco novo = enderecoMapper.toEntity(enderecoDto, cliente);
                resultantes.add(novo);
            }
        }

        // Os que sumiram são deletados pelo orphanRemoval = true:
        // basta limpar a lista atual e adicionar só os resultantes.
        cliente.getEnderecos().clear();
        cliente.getEnderecos().addAll(resultantes);

        ajustarPrincipal(cliente.getEnderecos());
    }

    /**
     * Normaliza o flag "principal" da lista:
     *   - Nenhum marcado → marca o primeiro
     *   - Mais de um marcado → mantém só o primeiro
     *   - null vira false
     */
    public void ajustarPrincipal(List<Endereco> enderecos) {
        if (enderecos == null || enderecos.isEmpty()) return;

        boolean jaAchouPrincipal = false;
        for (Endereco e : enderecos) {
            if (Boolean.TRUE.equals(e.getPrincipal()) && !jaAchouPrincipal) {
                e.setPrincipal(true);
                jaAchouPrincipal = true;
            } else {
                e.setPrincipal(false);
            }
        }

        if (!jaAchouPrincipal) {
            enderecos.get(0).setPrincipal(true);
        }
    }
}
