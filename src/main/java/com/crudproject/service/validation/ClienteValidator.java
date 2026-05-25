package com.crudproject.service.validation;

import br.com.caelum.stella.validation.CNPJValidator;
import br.com.caelum.stella.validation.CPFValidator;
import br.com.caelum.stella.validation.InvalidStateException;
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

// Centraliza as validações de negócio de Cliente.

@Component
public class ClienteValidator {

    @Autowired
    private ClienteRepository clienteRepository;

    public void validarCamposObrigatorios(ClienteDTO dto) {
        if (dto.getTipoPessoa() == null) {
            throw new RuntimeException("Tipo de pessoa é obrigatório.");
        }
        if (dto.getNome() == null || dto.getNome().isBlank()) {
            throw new RuntimeException("Nome/Razão Social é obrigatório.");
        }
        if (dto.getCpfCnpj() == null || dto.getCpfCnpj().isBlank()) {
            throw new RuntimeException("CPF/CNPJ é obrigatório.");
        }
        if (dto.getAtivo() == null) {
            throw new RuntimeException("O campo ativo é obrigatório.");
        }
        if (dto.getDataNascimento() == null) {
            String campo = dto.getTipoPessoa() == TipoPessoa.JURIDICA ? "Data de Fundação" : "Data de Nascimento";
            throw new RuntimeException(campo + " é obrigatória.");
        }

        // Validação de E-mail
        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new RuntimeException("E-mail é obrigatório.");
        }
        // Regex simples e eficiente para validar formato de e-mail
        if (!dto.getEmail().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            throw new RuntimeException("O formato do e-mail é inválido.");
        }

        // Obrigatoriedade do RG ou Inscrição Estadual
        if (dto.getTipoPessoa() == TipoPessoa.FISICA) {
            if (dto.getRgInscricaoEstadual() == null || dto.getRgInscricaoEstadual().isBlank()) {
                throw new RuntimeException("O RG é obrigatório para Pessoa Física.");
            }
        } else if (dto.getTipoPessoa() == TipoPessoa.JURIDICA) {
            if (dto.getRgInscricaoEstadual() == null || dto.getRgInscricaoEstadual().isBlank()) {
                throw new RuntimeException("A Inscrição Estadual é obrigatória para Pessoa Jurídica.");
            }
        }

        // Validação de sanidade do RG/IE — tamanho, caracteres permitidos e presença de números
        String rgIe = dto.getRgInscricaoEstadual();
        if (rgIe != null && !rgIe.isBlank()) {
            String nomeCampo = dto.getTipoPessoa() == TipoPessoa.JURIDICA ? "Inscrição Estadual" : "RG";
            if (rgIe.length() < 4 || rgIe.length() > 20) {
                throw new RuntimeException(nomeCampo + " deve ter entre 4 e 20 caracteres.");
            }
            if (!rgIe.matches("[a-zA-Z0-9.\\-/ ]+")) {
                throw new RuntimeException(nomeCampo + " contém caracteres inválidos. São permitidos apenas letras, números, pontos, traços, barras e espaços.");
            }
            // Não permite strings compostas exclusivamente por letras (ex: "ASDF")
            if (!rgIe.matches(".*\\d.*")) {
                throw new RuntimeException("Formato inválido. O " + nomeCampo + " não pode ser composto apenas por letras.");
            }
        }
    }

    public void validarDocumento(String cpfCnpjLimpo, TipoPessoa tipoPessoa) {
        if (cpfCnpjLimpo == null || cpfCnpjLimpo.isBlank()) return;

        try {
            if (tipoPessoa == TipoPessoa.FISICA) {
                new CPFValidator().assertValid(cpfCnpjLimpo);
            } else {
                new CNPJValidator().assertValid(cpfCnpjLimpo);
            }
        } catch (InvalidStateException e) {
            String tipo = tipoPessoa == TipoPessoa.FISICA ? "CPF" : "CNPJ";
            throw new RuntimeException("O " + tipo + " informado é inválido.");
        }
    }

    public void validarUnicidadeDocumento(String cpfCnpjLimpo, Long idAtual) {
        Optional<Cliente> existente = clienteRepository.findByCpfCnpj(cpfCnpjLimpo);
        if (existente.isPresent() && !Objects.equals(existente.get().getId(), idAtual)) {
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

            if (e.getNumero() == null || e.getNumero().isBlank()) {
                throw new RuntimeException("Número do endereço é obrigatório. Use 'SN' para endereços sem número.");
            }

            // Número aceita apenas dígitos (ex: "123") ou exatamente "SN" / "S/N".
            // Rejeita qualquer mistura de letras e números (ex: "12A", "12dasdasd").
            String numeroLimpo = e.getNumero().trim().toUpperCase();
            if (!numeroLimpo.matches("\\d+") && !numeroLimpo.equals("SN") && !numeroLimpo.equals("S/N")) {
                throw new RuntimeException("Número do endereço inválido. Use apenas números (ex: 123) ou 'SN' para sem número.");
            }

            if (e.getCep() == null || e.getCep().isBlank()) {
                throw new RuntimeException("CEP é obrigatório.");
            }
            if (e.getCep().length() != 8) {
                throw new RuntimeException("O CEP deve conter exatamente 8 dígitos.");
            }
            if (e.getCidade() == null || e.getCidade().isBlank()) {
                throw new RuntimeException("Cidade é obrigatória.");
            }
            if (e.getEstado() == null || e.getEstado().isBlank()) {
                throw new RuntimeException("Estado é obrigatório.");
            }
            if (e.getBairro() == null || e.getBairro().isBlank()) {
                throw new RuntimeException("Bairro é obrigatório.");
            }
            if (e.getPais() == null || e.getPais().isBlank()) {
                throw new RuntimeException("País é obrigatório.");
            }
            // Telefone é opcional; se informado, deve ter 10 ou 11 dígitos (com DDD)
            if (e.getTelefone() != null && !e.getTelefone().isBlank()) {
                if (e.getTelefone().length() < 10 || e.getTelefone().length() > 11) {
                    throw new RuntimeException("O telefone do endereço deve conter 10 ou 11 dígitos (incluindo o DDD).");
                }
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