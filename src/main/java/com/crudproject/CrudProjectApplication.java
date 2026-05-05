package com.crudproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Classe principal do projeto.
 *
 * @SpringBootApplication é uma anotação que combina três outras:
 *   - @Configuration      → indica que essa classe tem configurações do Spring
 *   - @EnableAutoConfiguration → o Spring configura automaticamente o que for necessário
 *   - @ComponentScan      → o Spring varre o pacote procurando classes para gerenciar
 *
 * O método main() é o ponto de entrada — é aqui que tudo começa quando rodamos o projeto.
 */

@SpringBootApplication
public class CrudProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrudProjectApplication.class, args);
    }
}
