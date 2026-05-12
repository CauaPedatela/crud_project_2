package com.crudproject.service.validation;

/**
 * Utilitário puro para validação matemática de CPF e CNPJ.
 *
 * É uma classe estática: não é gerenciada pelo Spring, não tem
 * dependências, não tem estado. Funciona como um conjunto de
 * "funções soltas" que qualquer parte do código pode chamar.
 *
 * Por que não @Component?
 * Validar dígitos não depende de banco, configuração ou contexto.
 * Métodos estáticos são mais simples e diretos para esse caso.
 *
 * Uso:
 *   if (!DocumentoUtil.isCpfValido(cpf)) { ... }
 */
public final class DocumentoUtil {

    // Construtor privado: impede que alguém faça "new DocumentoUtil()".
    // Não faz sentido instanciar — só usamos os métodos estáticos.
    private DocumentoUtil() {}

    /**
     * Valida CPF pelo algoritmo oficial da Receita Federal.
     *
     * Espera apenas os 11 dígitos (sem formatação).
     * Rejeita CPFs com todos os dígitos iguais (ex: 111.111.111-11).
     */
    public static boolean isCpfValido(String cpf) {
        if (cpf.matches("(\\d)\\1{10}")) return false;

        int soma = 0;
        for (int i = 0; i < 9; i++) soma += (cpf.charAt(i) - '0') * (10 - i);
        int primeiro = 11 - (soma % 11);
        if (primeiro >= 10) primeiro = 0;
        if (primeiro != (cpf.charAt(9) - '0')) return false;

        soma = 0;
        for (int i = 0; i < 10; i++) soma += (cpf.charAt(i) - '0') * (11 - i);
        int segundo = 11 - (soma % 11);
        if (segundo >= 10) segundo = 0;
        return segundo == (cpf.charAt(10) - '0');
    }

    /**
     * Valida CNPJ pelo algoritmo oficial da Receita Federal.
     *
     * Espera apenas os 14 dígitos (sem formatação).
     */
    public static boolean isCnpjValido(String cnpj) {
        if (cnpj.matches("(\\d)\\1{13}")) return false;

        int[] pesosPrimeiro = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] pesosSegundo = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

        int soma = 0;
        for (int i = 0; i < 12; i++) soma += (cnpj.charAt(i) - '0') * pesosPrimeiro[i];
        int primeiro = soma % 11 < 2 ? 0 : 11 - (soma % 11);
        if (primeiro != (cnpj.charAt(12) - '0')) return false;

        soma = 0;
        for (int i = 0; i < 13; i++) soma += (cnpj.charAt(i) - '0') * pesosSegundo[i];
        int segundo = soma % 11 < 2 ? 0 : 11 - (soma % 11);
        return segundo == (cnpj.charAt(13) - '0');
    }
}
