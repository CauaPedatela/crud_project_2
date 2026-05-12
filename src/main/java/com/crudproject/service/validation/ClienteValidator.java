package com.crudproject.service.validation;

import com.crudproject.dto.cliente.ClienteDTO;
import com.crudproject.dto.endereco.EnderecoDTO;
import com.crudproject.model.Cliente;
import com.crudproject.model.TipoPessoa;
import com.crudproject.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Centraliza as validações de negócio de Cliente.
 *
 * O Service orquestra as operações (salvar, atualizar) e delega
 * as verificações de regras para esse validador. Mantém o Service
 * mais enxuto e facilita testes isolados das regras.
 *
 * É @Component porque depende de ClienteRepository (precisa do banco
 * para checar unicidade de documento).
 */
@Component
public class ClienteValidator {

    @Autowired
    private ClienteRepository clienteRepository;

    public void validarCamposObrigatorios(ClienteDTO dto) {
        if (dto.getTipoPessoa() == null) {
            throw new RuntimeException("Tipo de pessoa é obrigatório.");
        }
        if (dto.getNome() == null || dto.getNome().isBlank()) {
            throw new RuntimeException("Nome é obrigatório.");
        }
        if (dto.getCpfCnpj() == null || dto.getCpfCnpj().isBlank()) {
            throw new RuntimeException("CPF/CNPJ é obrigatório.");
        }
        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new RuntimeException("E-mail é obrigatório.");
        }
        if (dto.getAtivo() == null) {
            throw new RuntimeException("O campo ativo é obrigatório.");
        }
    }

    public void validarDocumento(String cpfCnpj, TipoPessoa tipoPessoa) {
        String digitos = cpfCnpj.replaceAll("[^0-9]", "");

        if (tipoPessoa == TipoPessoa.FISICA) {
            if (digitos.length() != 11) {
                throw new RuntimeException("CPF deve ter 11 dígitos.");
            }
            if (!DocumentoUtil.isCpfValido(digitos)) {
                throw new RuntimeException("CPF inválido.");
            }
        } else {
            if (digitos.length() != 14) {
                throw new RuntimeException("CNPJ deve ter 14 dígitos.");
            }
            if (!DocumentoUtil.isCnpjValido(digitos)) {
                throw new RuntimeException("CNPJ inválido.");
            }
        }
    }

    public void validarUnicidadeDocumento(String cpfCnpj, Long idAtual) {
        Optional<Cliente> existente = clienteRepository.findByCpfCnpj(cpfCnpj);
        if (existente.isPresent()
                && !Objects.equals(existente.get().getId(), idAtual)) {
            throw new RuntimeException("CPF/CNPJ já cadastrado para outro cliente.");
        }
    }

    public void validarEnderecos(List<EnderecoDTO> enderecos) {
        if (enderecos == null || enderecos.isEmpty()) {
            throw new RuntimeException("Cliente deve ter pelo menos um endereço.");
        }
        for (EnderecoDTO e : enderecos) {
            if (e.getTipo() == null) {
                throw new RuntimeException("Tipo do endereço é obrigatório.");
            }
            if (e.getLogradouro() == null || e.getLogradouro().isBlank()) {
                throw new RuntimeException("Logradouro é obrigatório.");
            }
            if (e.getCep() == null || e.getCep().isBlank()) {
                throw new RuntimeException("CEP é obrigatório.");
            }
            if (e.getCidade() == null || e.getCidade().isBlank()) {
                throw new RuntimeException("Cidade é obrigatória.");
            }
            if (e.getEstado() == null || e.getEstado().isBlank()) {
                throw new RuntimeException("Estado é obrigatório.");
            }
            if (e.getPais() == null || e.getPais().isBlank()) {
                throw new RuntimeException("País é obrigatório.");
            }
        }
    }

    public void validarTipoPessoaImutavel(Cliente existente, ClienteDTO dto) {
        if (dto.getTipoPessoa() != null
                && !Objects.equals(existente.getTipoPessoa(), dto.getTipoPessoa())) {
            throw new RuntimeException("Não é permitido alterar o tipo de pessoa.");
        }
    }
}
