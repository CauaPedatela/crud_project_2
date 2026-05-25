package com.crudproject.service.validation;

// Formata valores limpos (só dígitos) para exibição na UI.
// Complemento do DocumentoUtil, que faz o caminho inverso (remove formatação para validar/salvar).
// Métodos tolerantes: retornam o valor original se não corresponder ao padrão esperado.

public final class MascaraUtil {

    private MascaraUtil() {}

    // CPF: "12345678901"         → "123.456.789-01"
    // CNPJ: "12345678000195"     → "12.345.678/0001-95"
    public static String formatarCpfCnpj(String valor) {
        if (valor == null || valor.isBlank()) return valor != null ? valor : "";
        String d = valor.replaceAll("\\D", "");
        if (d.length() == 11) {
            return d.replaceAll("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");
        }
        if (d.length() == 14) {
            return d.replaceAll("(\\d{2})(\\d{3})(\\d{3})(\\d{4})(\\d{2})", "$1.$2.$3/$4-$5");
        }
        return valor;
    }

    // CEP: "01001000"  → "01001-000"
    public static String formatarCep(String valor) {
        if (valor == null || valor.isBlank()) return valor != null ? valor : "";
        String d = valor.replaceAll("\\D", "");
        if (d.length() == 8) {
            return d.replaceAll("(\\d{5})(\\d{3})", "$1-$2");
        }
        return valor;
    }

    // Fixo  (10 dígitos): "1133334444" → "(11) 3333-4444"
    // Celular (11 dígitos): "11987654321" → "(11) 98765-4321"
    public static String formatarTelefone(String valor) {
        if (valor == null || valor.isBlank()) return valor != null ? valor : "";
        String d = valor.replaceAll("\\D", "");
        if (d.length() == 10) {
            return d.replaceAll("(\\d{2})(\\d{4})(\\d{4})", "($1) $2-$3");
        }
        if (d.length() == 11) {
            return d.replaceAll("(\\d{2})(\\d{5})(\\d{4})", "($1) $2-$3");
        }
        return valor;
    }
}
