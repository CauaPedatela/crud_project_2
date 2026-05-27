package com.crudproject.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

// Tratador global de exceções da camada REST.
//
// Sem este handler, quando o ClienteValidator (ou qualquer Service) lança uma
// RuntimeException, o Spring devolve o erro 500 padrão sem expor a mensagem
// no corpo da resposta — o frontend então só consegue exibir "Erro genérico".
//
// Aqui interceptamos RuntimeException e IllegalArgumentException e devolvemos
// um JSON com o campo "message" preenchido, que o Angular lê em err.error.message.

@ControllerAdvice
public class GlobalExceptionHandler {

    // Captura exceções de regra de negócio (validações do ClienteValidator,
    // CPF duplicado, cliente não encontrado, endereço inválido, etc.).
    // Devolve status 400 (Bad Request) com a mensagem real para o usuário ver.
    @ExceptionHandler({ RuntimeException.class, IllegalArgumentException.class })
    public ResponseEntity<Map<String, Object>> handleNegocio(RuntimeException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}
