package com.crudproject.config;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;

// Tratador global de exceções da camada REST.
//
// Sem este handler, quando o ClienteValidator (ou qualquer Service) lança uma
// RuntimeException, o Spring devolve o erro 500 padrão sem expor a mensagem
// no corpo da resposta — o frontend então só consegue exibir "Erro genérico".
//
// Aqui interceptamos os casos mais comuns e devolvemos um JSON com o campo
// "message" preenchido, que o Angular lê em err.error.message.

@ControllerAdvice
public class GlobalExceptionHandler {

    // ============================================================
    // DATAS INVÁLIDAS — vêm como JSON inválido do Jackson
    // ============================================================
    //
    // Quando o JSON traz uma data fora do formato (ex: "9999-99-99" ou texto
    // qualquer), o Jackson dispara HttpMessageNotReadableException antes
    // mesmo de chegar ao Controller. Sem este handler, o frontend recebia
    // uma mensagem horrorosa tipo:
    //   "JSON parse error: Cannot deserialize value of type
    //    `java.time.LocalDate` from String "9999-99-99"..."
    //
    // Aqui detectamos a causa (InvalidFormatException sobre LocalDate) e
    // devolvemos uma mensagem amigável ao usuário.
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleJsonInvalido(HttpMessageNotReadableException ex) {
        // Procura na cadeia de causas se o problema foi uma data inválida
        Throwable causa = ex.getCause();
        while (causa != null) {
            if (causa instanceof InvalidFormatException) {
                InvalidFormatException ife = (InvalidFormatException) causa;
                if (ife.getTargetType() == LocalDate.class) {
                    return responseDataInvalida();
                }
            }
            if (causa instanceof DateTimeParseException) {
                return responseDataInvalida();
            }
            causa = causa.getCause();
        }

        // Outro problema de JSON (campo errado, payload mal formado, etc.)
        return badRequest("Requisição mal formada. Verifique os dados enviados.");
    }

    // Captura DateTimeParseException disparada diretamente pelo Service
    // (por exemplo, em LocalDate.parse() dos filtros de data).
    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<Map<String, Object>> handleDataInvalida(DateTimeParseException ex) {
        return responseDataInvalida();
    }

    // ============================================================
    // REGRAS DE NEGÓCIO — RuntimeException / IllegalArgumentException
    // ============================================================
    //
    // Captura exceções de regra de negócio (validações do ClienteValidator,
    // CPF duplicado, cliente não encontrado, endereço inválido, etc.).
    // Devolve status 400 (Bad Request) com a mensagem real para o usuário ver.
    @ExceptionHandler({ RuntimeException.class, IllegalArgumentException.class })
    public ResponseEntity<Map<String, Object>> handleNegocio(RuntimeException ex) {
        return badRequest(ex.getMessage());
    }

    // ============================================================
    // Helpers privados
    // ============================================================

    private ResponseEntity<Map<String, Object>> responseDataInvalida() {
        return badRequest("Data inválida. Use o formato dd/mm/aaaa com dia/mês/ano válidos.");
    }

    private ResponseEntity<Map<String, Object>> badRequest(String mensagem) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("message", mensagem);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}
