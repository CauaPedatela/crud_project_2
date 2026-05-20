package com.crudproject.service.validation;

// Utilitário puro para validação matemática de CPF e CNPJ.
// É uma classe estática: não é gerenciada pelo Spring, não tem
// dependências, não tem estado. É um conjunto de funções soltas
// que podem ser chamadas em qualquer parte do código.

public final class DocumentoUtil {

    private DocumentoUtil() {}

    // Remove qualquer caractere que não seja número (pontos, traços, barras, espaços)
    public static String limparFormatacao(String valor) {
        if (valor == null) {
            return null;
        }
        return valor.replaceAll("\\D", "");
    }
}
