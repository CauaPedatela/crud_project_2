package com.crudproject.service;

import com.crudproject.dto.cliente.ClienteCadastroDTO;
import com.crudproject.dto.cliente.ClienteResponseDTO;
import com.crudproject.mapper.ClienteMapper;
import com.crudproject.model.Cliente;
import com.crudproject.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ClienteMapper clienteMapper;

    // ===================================================
    // MÉTODOS PÚBLICOS (usados pela página Wicket)
    // ===================================================

    @Transactional
    public ClienteResponseDTO salvar(ClienteCadastroDTO dto) {

        // Converte DTO em entidade (sem id ainda)
        Cliente cliente = clienteMapper.toEntity(dto);

        // Valida campos comuns a PF e PJ (inclui tipo de pessoa)
        validarCamposComuns(cliente);

        if (cliente.isPessoaFisica()) {
            // Valida campos obrigatórios de PF
            validarCamposPessoaFisica(cliente);

            // Valida se CPF é válido
            if (!isCpfValido(cliente.getCpf())) {
                throw new RuntimeException("CPF inválido.");
            }

            // Verifica duplicidade de CPF
            if (clienteRepository.findByCpf(cliente.getCpf()).isPresent()) {
                throw new RuntimeException("CPF já cadastrado.");
            }
        }

        if (cliente.isPessoaJuridica()) {
            // Valida campos obrigatórios de PJ
            validarCamposPessoaJuridica(cliente);

            // Valida se CNPJ é válido
            if (!isCnpjValido(cliente.getCnpj())) {
                throw new RuntimeException("CNPJ inválido.");
            }

            // Verifica duplicidade de CNPJ
            if (clienteRepository.findByCnpj(cliente.getCnpj()).isPresent()) {
                throw new RuntimeException("CNPJ já cadastrado.");
            }
        }

        // Persiste no banco e retorna a versão salva (já com id)
        Cliente salvo = clienteRepository.save(cliente);

        // Converte a entidade salva em DTO de saída
        return clienteMapper.toResponse(salvo);
    }

    public List<Cliente> buscarTodos() {
        return clienteRepository.findAll();
    }

    public Cliente buscarPorId(Long id) {
        Optional<Cliente> cliente = clienteRepository.findById(id);
        if (!cliente.isPresent()) {
            throw new RuntimeException("Cliente não encontrado.");
        }
        return cliente.get();
    }

    @Transactional
    public Cliente atualizar(Cliente cliente) {

        // Verifica se o cliente existe no banco
        if (cliente.getId() == null || !clienteRepository.existsById(cliente.getId())) {
            throw new RuntimeException("Cliente não encontrado.");
        }

        // Valida campos comuns a PF e PJ (inclui tipo de pessoa)
        validarCamposComuns(cliente);

        // Verifica se o tipo de pessoa foi alterado — deve ser a primeira
        // validação após confirmar que o cliente existe, pois mudar de PF
        // para PJ (ou vice-versa) corromperia os dados do banco.
        Cliente clienteExistente = buscarPorId(cliente.getId());
        if (!clienteExistente.getTipoPessoa().equals(cliente.getTipoPessoa())) {
            throw new RuntimeException("Não é permitido alterar o tipo de pessoa.");
        }

        if (cliente.isPessoaFisica()) {
            // Valida campos obrigatórios de PF
            validarCamposPessoaFisica(cliente);

            // Valida se CPF é válido
            if (!isCpfValido(cliente.getCpf())) {
                throw new RuntimeException("CPF inválido.");
            }

            // Verifica se o CPF pertence a outro cliente
            Optional<Cliente> existente = clienteRepository.findByCpf(cliente.getCpf());
            if (existente.isPresent() && !existente.get().getId().equals(cliente.getId())) {
                throw new RuntimeException("CPF já cadastrado para outro cliente.");
            }
        }

        if (cliente.isPessoaJuridica()) {
            // Valida campos obrigatórios de PJ
            validarCamposPessoaJuridica(cliente);

            // Valida se CNPJ é válido
            if (!isCnpjValido(cliente.getCnpj())) {
                throw new RuntimeException("CNPJ inválido.");
            }

            // Verifica se o CNPJ pertence a outro cliente
            Optional<Cliente> existente = clienteRepository.findByCnpj(cliente.getCnpj());
            if (existente.isPresent() && !existente.get().getId().equals(cliente.getId())) {
                throw new RuntimeException("CNPJ já cadastrado para outro cliente.");
            }
        }

        return clienteRepository.save(cliente);
    }

    @Transactional
    public void excluir(Long id) {
        if (!clienteRepository.existsById(id)) {
            throw new RuntimeException("Cliente não encontrado.");
        }
        clienteRepository.deleteById(id);
    }

    // ===================================================
    // MÉTODOS PRIVADOS (auxiliares — só usados aqui dentro)
    // ===================================================

    /**
     * Valida campos obrigatórios exclusivos de Pessoa Física.
     * Chamado tanto no salvar() quanto no atualizar().
     */
    private void validarCamposPessoaFisica(Cliente cliente) {
        if (cliente.getCpf() == null || cliente.getCpf().isBlank()) {
            throw new RuntimeException("CPF é obrigatório.");
        }
        if (cliente.getNome() == null || cliente.getNome().isBlank()) {
            throw new RuntimeException("Nome é obrigatório.");
        }
        if (cliente.getRg() == null || cliente.getRg().isBlank()) {
            throw new RuntimeException("RG é obrigatório.");
        }
        if (cliente.getDataNascimento() == null) {
            throw new RuntimeException("Data de nascimento é obrigatória.");
        }
    }

    /**
     * Valida campos obrigatórios exclusivos de Pessoa Jurídica.
     * Chamado tanto no salvar() quanto no atualizar().
     */
    private void validarCamposPessoaJuridica(Cliente cliente) {
        if (cliente.getCnpj() == null || cliente.getCnpj().isBlank()) {
            throw new RuntimeException("CNPJ é obrigatório.");
        }
        if (cliente.getRazaoSocial() == null || cliente.getRazaoSocial().isBlank()) {
            throw new RuntimeException("Razão Social é obrigatória.");
        }
        if (cliente.getInscricaoEstadual() == null || cliente.getInscricaoEstadual().isBlank()) {
            throw new RuntimeException("Inscrição Estadual é obrigatória.");
        }
        if (cliente.getDataCriacao() == null) {
            throw new RuntimeException("Data de criação é obrigatória.");
        }
    }

    /**
     * Valida campos obrigatórios comuns a PF e PJ.
     * Chamado tanto no salvar() quanto no atualizar().
     */
    private void validarCamposComuns(Cliente cliente) {
        if (cliente.getTipoPessoa() == null) {
            throw new RuntimeException("Selecione o tipo de pessoa!");
        }
        if (cliente.getEmail() == null || cliente.getEmail().isBlank()) {
            throw new RuntimeException("E-mail é obrigatório.");
        }
        if (cliente.getAtivo() == null) {
            throw new RuntimeException("O campo ativo é obrigatório.");
        }
    }

    /**
     * Valida se um CPF é matematicamente válido.
     *
     * Remove a formatação (pontos e traço), verifica os dígitos
     * verificadores usando o algoritmo oficial da Receita Federal.
     *
     * Exemplo de CPF válido: 529.982.247-25
     */
    private boolean isCpfValido(String cpf) {
        // Remove formatação (pontos e traço)
        cpf = cpf.replaceAll("[^0-9]", "");

        // CPF deve ter 11 dígitos
        if (cpf.length() != 11) return false;

        // Rejeita CPFs com todos os dígitos iguais (ex: 111.111.111-11)
        if (cpf.matches("(\\d)\\1{10}")) return false;

        // Calcula e verifica o primeiro dígito verificador
        int soma = 0;
        for (int i = 0; i < 9; i++) soma += (cpf.charAt(i) - '0') * (10 - i);
        int primeiro = 11 - (soma % 11);
        if (primeiro >= 10) primeiro = 0;
        if (primeiro != (cpf.charAt(9) - '0')) return false;

        // Calcula e verifica o segundo dígito verificador
        soma = 0;
        for (int i = 0; i < 10; i++) soma += (cpf.charAt(i) - '0') * (11 - i);
        int segundo = 11 - (soma % 11);
        if (segundo >= 10) segundo = 0;
        return segundo == (cpf.charAt(10) - '0');
    }

    /**
     * Valida se um CNPJ é matematicamente válido.
     *
     * Remove a formatação, verifica os dígitos verificadores
     * usando o algoritmo oficial da Receita Federal.
     *
     * Exemplo de CNPJ válido: 11.222.333/0001-81
     */
    private boolean isCnpjValido(String cnpj) {
        // Remove formatação
        cnpj = cnpj.replaceAll("[^0-9]", "");

        // CNPJ deve ter 14 dígitos
        if (cnpj.length() != 14) return false;

        // Rejeita CNPJs com todos os dígitos iguais
        if (cnpj.matches("(\\d)\\1{13}")) return false;

        // Pesos para cálculo dos dígitos verificadores
        int[] pesosPrimeiro  = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] pesosSegundo = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

        // Calcula e verifica o primeiro dígito verificador
        int soma = 0;
        for (int i = 0; i < 12; i++) soma += (cnpj.charAt(i) - '0') * pesosPrimeiro[i];
        int primeiro = soma % 11 < 2 ? 0 : 11 - (soma % 11);
        if (primeiro != (cnpj.charAt(12) - '0')) return false;

        // Calcula e verifica o segundo dígito verificador
        soma = 0;
        for (int i = 0; i < 13; i++) soma += (cnpj.charAt(i) - '0') * pesosSegundo[i];
        int segundo = soma % 11 < 2 ? 0 : 11 - (soma % 11);
        return segundo == (cnpj.charAt(13) - '0');
    }
}
