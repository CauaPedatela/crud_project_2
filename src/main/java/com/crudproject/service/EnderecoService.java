package com.crudproject.service;

import com.crudproject.dto.endereco.EnderecoAtualizacaoDTO;
import com.crudproject.dto.endereco.EnderecoCadastroDTO;
import com.crudproject.dto.endereco.EnderecoResponseDTO;
import com.crudproject.mapper.EnderecoMapper;
import com.crudproject.model.Cliente;
import com.crudproject.model.Endereco;
import com.crudproject.repository.ClienteRepository;
import com.crudproject.repository.EnderecoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EnderecoService {

    @Autowired
    private EnderecoRepository enderecoRepository;

    // Necessário para verificar se o cliente existe antes de salvar um endereço
    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private EnderecoMapper enderecoMapper;

    // ===================================================
    // MÉTODOS PÚBLICOS (consumidos por Controller e Wicket)
    //
    // Todos recebem e devolvem DTOs — a entidade Endereco
    // nunca sai dessa camada.
    // ===================================================

    @Transactional
    public EnderecoResponseDTO salvar(EnderecoCadastroDTO dto) {

        // Valida que o DTO traz um clienteId e que esse cliente existe
        Cliente cliente = validarClienteExiste(dto.getClienteId());

        // Valida campos obrigatórios do endereço
        validarCamposObrigatorios(dto);

        // Converte DTO em entidade
        Endereco endereco = enderecoMapper.toEntity(dto, cliente);

        // Regras do endereço principal:
        //  - Se for o primeiro endereço do cliente → vira principal automaticamente
        //  - Se o usuário marcou como principal → desmarca os outros do mesmo cliente
        //  - Se não marcou nada → trata como false
        List<Endereco> existentes = enderecoRepository.findByClienteId(cliente.getId());
        boolean ehPrimeiro = existentes.isEmpty();
        boolean querSerPrincipal = Boolean.TRUE.equals(dto.getEnderecoPrincipal());

        if (ehPrimeiro) {
            endereco.setEnderecoPrincipal(true);
        } else if (querSerPrincipal) {
            desmarcarOutrosPrincipais(existentes, null);
            endereco.setEnderecoPrincipal(true);
        } else {
            endereco.setEnderecoPrincipal(false);
        }

        Endereco salvo = enderecoRepository.save(endereco);
        return enderecoMapper.toResponse(salvo);
    }

    public EnderecoResponseDTO buscarPorId(Long id) {
        return enderecoMapper.toResponse(buscarEntidadePorId(id));
    }

    public List<EnderecoResponseDTO> buscarPorCliente(Long clienteId) {
        return enderecoRepository.findByClienteId(clienteId)
                .stream()
                .map(enderecoMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public EnderecoResponseDTO atualizar(Long id, EnderecoAtualizacaoDTO dto) {

        // Carrega o endereço existente — lança se não achar
        Endereco endereco = buscarEntidadePorId(id);

        // OBS: não validamos mais "não pode mover o endereço para outro cliente"
        // aqui porque o EnderecoAtualizacaoDTO não tem clienteId. O DTO em si
        // já não permite essa operação — o vínculo com o cliente é mantido
        // implicitamente pelo próprio mapper (updateEntity preserva cliente).

        // Valida campos obrigatórios do endereço
        validarCamposObrigatorios(dto);

        Long clienteAtualId = endereco.getCliente().getId();

        // Guarda o estado anterior do flag principal antes de sobrescrever
        boolean eraPrincipal = Boolean.TRUE.equals(endereco.getEnderecoPrincipal());
        boolean querSerPrincipal = Boolean.TRUE.equals(dto.getEnderecoPrincipal());

        // Sobrescreve os campos do endereço com os valores do DTO.
        // Cliente e id ficam preservados (updateEntity não mexe neles).
        enderecoMapper.updateEntity(endereco, dto);

        // Normaliza o flag: null vira false (a coluna no banco é NOT NULL).
        endereco.setEnderecoPrincipal(querSerPrincipal);

        // Se passou a ser principal agora, desmarca os outros do mesmo cliente
        if (querSerPrincipal && !eraPrincipal) {
            List<Endereco> outros = enderecoRepository.findByClienteId(clienteAtualId);
            desmarcarOutrosPrincipais(outros, id);
        }

        Endereco salvo = enderecoRepository.save(endereco);
        return enderecoMapper.toResponse(salvo);
    }

    @Transactional
    public void excluir(Long id) {
        if (!enderecoRepository.existsById(id)) {
            throw new RuntimeException("Endereço não encontrado.");
        }
        enderecoRepository.deleteById(id);
    }

    /**
     * Promove um endereço a "principal" e desmarca os demais do mesmo cliente.
     * Toda a operação acontece dentro da mesma transação para garantir que
     * o cliente nunca fique sem principal ou com mais de um.
     */
    @Transactional
    public void definirComoPrincipal(Long id) {
        Endereco alvo = buscarEntidadePorId(id);
        Long clienteId = alvo.getCliente().getId();

        List<Endereco> doCliente = enderecoRepository.findByClienteId(clienteId);

        for (Endereco e : doCliente) {
            e.setEnderecoPrincipal(e.getId().equals(id));
        }

        enderecoRepository.saveAll(doCliente);
    }

    // ===================================================
    // MÉTODOS PRIVADOS (auxiliares — só usados aqui dentro)
    // ===================================================

    /**
     * Busca a entidade Endereco pelo id ou lança exceção.
     *
     * Privado porque a entidade não deve vazar para fora do Service.
     */
    private Endereco buscarEntidadePorId(Long id) {
        Optional<Endereco> endereco = enderecoRepository.findById(id);
        if (!endereco.isPresent()) {
            throw new RuntimeException("Endereço não encontrado.");
        }
        return endereco.get();
    }

    /**
     * Valida campos obrigatórios do Endereço diretamente do DTO de cadastro.
     * Chamado no salvar().
     */
    private void validarCamposObrigatorios(EnderecoCadastroDTO dto) {
        if (dto.getLogradouro() == null || dto.getLogradouro().isBlank()) {
            throw new RuntimeException("Logradouro é obrigatório.");
        }
        if (dto.getCep() == null || dto.getCep().isBlank()) {
            throw new RuntimeException("CEP é obrigatório.");
        }
        if (dto.getCidade() == null || dto.getCidade().isBlank()) {
            throw new RuntimeException("Cidade é obrigatória.");
        }
        if (dto.getEstado() == null || dto.getEstado().isBlank()) {
            throw new RuntimeException("Estado é obrigatório.");
        }
        // enderecoPrincipal não é validado aqui pois o sistema controla
        // automaticamente: o primeiro endereço sempre vira principal,
        // e a troca é feita pelo método definirComoPrincipal()
    }

    /**
     * Sobrecarga (overload) do mesmo método de validação para o DTO de
     * atualização. Mesmas regras de campos obrigatórios.
     *
     * Em Java, dois métodos podem ter o mesmo nome se tiverem
     * parâmetros diferentes — o compilador escolhe qual chamar
     * baseado no tipo passado.
     */
    private void validarCamposObrigatorios(EnderecoAtualizacaoDTO dto) {
        if (dto.getLogradouro() == null || dto.getLogradouro().isBlank()) {
            throw new RuntimeException("Logradouro é obrigatório.");
        }
        if (dto.getCep() == null || dto.getCep().isBlank()) {
            throw new RuntimeException("CEP é obrigatório.");
        }
        if (dto.getCidade() == null || dto.getCidade().isBlank()) {
            throw new RuntimeException("Cidade é obrigatória.");
        }
        if (dto.getEstado() == null || dto.getEstado().isBlank()) {
            throw new RuntimeException("Estado é obrigatório.");
        }
    }

    /**
     * Verifica se o clienteId é válido e retorna a entidade Cliente.
     * Um endereço sem cliente válido não pode ser salvo.
     */
    private Cliente validarClienteExiste(Long clienteId) {
        if (clienteId == null) {
            throw new RuntimeException("Endereço deve estar vinculado a um cliente.");
        }
        Optional<Cliente> cliente = clienteRepository.findById(clienteId);
        if (!cliente.isPresent()) {
            throw new RuntimeException("Cliente não encontrado.");
        }
        return cliente.get();
    }

    /**
     * Desmarca o flag enderecoPrincipal de todos os endereços da lista,
     * exceto o de id informado em "exceto" (pode ser null para desmarcar todos).
     *
     * Não chama save() — quem chamou se encarrega da persistência dentro
     * da mesma transação.
     */
    private void desmarcarOutrosPrincipais(List<Endereco> enderecos, Long exceto) {
        for (Endereco e : enderecos) {
            if (exceto != null && e.getId().equals(exceto)) continue;
            e.setEnderecoPrincipal(false);
        }
        enderecoRepository.saveAll(enderecos);
    }

}
