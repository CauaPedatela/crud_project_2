package com.crudproject.model;

/**
 * Enum que representa o tipo de pessoa de um Cliente.
 *
 * Enum é um tipo especial de classe que define todos os valores
 * possíveis para um campo, evitando erros de digitação e
 * valores inválidos.
 *
 * Uso:
 *   cliente.setTipoPessoa(TipoPessoa.FISICA);
 *
 *   if (cliente.isPessoaFisica()) { ... }
 */
public enum TipoPessoa {

    FISICA,    // Pessoa Física  → campos: CPF, RG, Data de Nascimento
    JURIDICA   // Pessoa Jurídica → campos: CNPJ, Razão Social, Inscrição Estadual
}
