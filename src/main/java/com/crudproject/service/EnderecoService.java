package com.crudproject.service;

import com.crudproject.model.Endereco;
import com.crudproject.repository.ClienteRepository;
import com.crudproject.repository.EnderecoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class EnderecoService {

    @Autowired
    private EnderecoRepository enderecoRepository;

    // Necessário para verificar se o cliente existe antes de salvar um endereço
    @Autowired
    private ClienteRepository clienteRepository;

    // ===================================================
    // MÉTODOS PÚBLICOS (usados pela página Wicket)
    // ===================================================

    @Transactional
    public Endereco salvar(Endereco endereco) {

        return endereco;
    }

    public Endereco buscarPorId(Long id) {
        Optional<Endereco> endereco = enderecoRepository.findById(id);
        if (!endereco.isPresent()) {
            throw new RuntimeException("Endereço não encontrado.");
        }
        return endereco.get();
    }

    public List<Endereco> buscarPorCliente(Long clienteId) {
        return enderecoRepository.findByClienteId(clienteId);
    }

    @Transactional
    public Endereco atualizar(Endereco endereco) {

        return endereco;
    }

    @Transactional
    public void excluir(Long id) {

    }

    @Transactional
    public void definirComoPrincipal(Long id) {

    }

    // ===================================================
    // MÉTODOS PRIVADOS (auxiliares — só usados aqui dentro)
    // ===================================================

    /**
     * Valida campos obrigatórios do Endereço.
     * Chamado tanto no salvar() quanto no atualizar().
     */
    private void validarCamposObrigatorios(Endereco endereco) {
        if (endereco.getLogradouro() == null || endereco.getLogradouro().isBlank()) {
            throw new RuntimeException("Logradouro é obrigatório.");
        }
        if (endereco.getCep() == null || endereco.getCep().isBlank()) {
            throw new RuntimeException("CEP é obrigatório.");
        }
        if (endereco.getCidade() == null || endereco.getCidade().isBlank()) {
            throw new RuntimeException("Cidade é obrigatória.");
        }
        if (endereco.getEstado() == null || endereco.getEstado().isBlank()) {
            throw new RuntimeException("Estado é obrigatório.");
        }
        // enderecoPrincipal não é validado aqui pois o sistema controla
        // automaticamente: o primeiro endereço sempre vira principal,
        // e a troca é feita pelo método definirComoPrincipal()
    }

    /**
     * Verifica se o cliente vinculado ao endereço existe no banco.
     * Um endereço sem cliente válido não pode ser salvo.
     */
    private void validarClienteExiste(Endereco endereco) {
        if (endereco.getCliente() == null || endereco.getCliente().getId() == null) {
            throw new RuntimeException("Endereço deve estar vinculado a um cliente.");
        }
        if (!clienteRepository.existsById(endereco.getCliente().getId())) {
            throw new RuntimeException("Cliente não encontrado.");
        }
    }

}
