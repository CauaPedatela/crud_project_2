# Desafio de Estágio — CRUD de Clientes

## O que é esse projeto?

Sistema para cadastrar e gerenciar **Clientes e Endereços**, desenvolvido como desafio de estágio.

O usuário pode: **criar, editar, excluir e listar** clientes, além de gerar relatórios em PDF e Excel.

---

## Arquitetura do sistema

```
FRONTEND (o que o usuário vê)
  ├── Wicket 7   → Fase 1 (implementar primeiro)
  └── Angular 14 → Fase 2 (migrar depois)
        ↕  HTTP (chamadas REST)
BACKEND (Spring Boot - Java 17)
  ├── Controller → recebe as requisições
  ├── Service    → aplica as regras de negócio
  └── DAO/Repository → acessa o banco de dados
        ↕  Hibernate (JPA)
BANCO DE DADOS (MySQL)
```

**Padrão MVC:**
- **M** (Model): classes Java que representam os dados (Cliente, Endereço)
- **V** (View): telas do usuário (Wicket ou Angular)
- **C** (Controller): código que recebe as requisições e decide o que fazer

---

## Tecnologias

| Tecnologia | Para que serve |
|---|---|
| Java 17 | Linguagem do backend |
| Spring Boot | Framework que facilita criar APIs em Java |
| Hibernate 5 | Converte objetos Java em tabelas do banco |
| MySQL | Banco de dados |
| Apache Wicket 7 | Framework para criar telas web em Java (Fase 1) |
| Angular 14 | Framework frontend moderno (Fase 2) |
| JasperReports | Geração de relatórios em PDF |
| Apache POI | Geração e leitura de planilhas Excel |
| JUnit | Testes automatizados |

---

## Modelos de dados

### Cliente
| Campo | Tipo | Observação |
|---|---|---|
| tipoPessoa | Enum | FISICA ou JURIDICA |
| cpf | String | Só para Pessoa Física |
| nome | String | Só para Pessoa Física |
| rg | String | Só para Pessoa Física |
| dataNascimento | LocalDate | Só para Pessoa Física |
| cnpj | String | Só para Pessoa Jurídica |
| razaoSocial | String | Só para Pessoa Jurídica |
| inscricaoEstadual | String | Só para Pessoa Jurídica |
| dataCriacao | LocalDate | Só para Pessoa Jurídica |
| email | String | Todos |
| ativo | Boolean | Sim ou Não |
| enderecos | List\<Endereco\> | Relação 1:N (um cliente, vários endereços) |

### Endereço
| Campo | Tipo |
|---|---|
| logradouro | String |
| numero | String |
| cep | String |
| bairro | String |
| telefone | String |
| cidade | String |
| estado | String |
| enderecoPrincipal | Boolean |
| complemento | String |

---

## Roteiro de desenvolvimento

### ✅ Fase 0 — Preparação (PRÓXIMO PASSO)

Antes de escrever código, verifique as ferramentas. Abra o terminal e rode cada comando:

```bash
# 1. Verificar Java 17
java -version
# Esperado: java version "17.x.x"

# 2. Verificar Maven
mvn -version
# Esperado: Apache Maven 3.x.x

# 3. Verificar Git
git --version
# Esperado: git version 2.x.x

# 4. Verificar MySQL (se instalado)
mysql --version
```

**Se algum estiver faltando:**
- JDK 17: https://adoptium.net/temurin/releases/?version=17
- Maven: https://maven.apache.org/download.cgi
- MySQL: https://dev.mysql.com/downloads/installer/

---

### Fase 1 — Recriar o projeto como Spring Boot

O `pom.xml` atual está quase vazio. Precisamos transformá-lo em um projeto Spring Boot real.

O `pom.xml` novo deve ter:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <!-- Herda configurações do Spring Boot -->
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.5</version>
    </parent>

    <groupId>com.crudproject</groupId>
    <artifactId>crud-project</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>crud-project</name>

    <properties>
        <java.version>17</java.version>
    </properties>

    <dependencies>
        <!-- Spring Boot Web: para criar APIs REST -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Spring Data JPA + Hibernate: para acessar o banco -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <!-- MySQL: driver de conexão com o banco -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- Testes com JUnit -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

---

### Fase 2 — Estrutura de pastas

Dentro de `src/main/java/com/crudproject/` criar:

```
src/
└── main/
    ├── java/
    │   └── com/crudproject/
    │       ├── CrudProjectApplication.java  ← ponto de entrada
    │       ├── model/
    │       │   ├── Cliente.java
    │       │   └── Endereco.java
    │       ├── repository/
    │       │   ├── ClienteRepository.java
    │       │   └── EnderecoRepository.java
    │       ├── service/
    │       │   ├── ClienteService.java
    │       │   └── EnderecoService.java
    │       └── controller/
    │           ├── ClienteController.java
    │           └── EnderecoController.java
    └── resources/
        └── application.properties  ← configuração do banco
```

---

### Fase 3 — Configurar o banco de dados

No arquivo `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/crud_clientes?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=SUA_SENHA_AQUI
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

> Substitua `SUA_SENHA_AQUI` pela senha do seu MySQL local.

---

### Fase 4 — Criar os Modelos (model/)

Os modelos são classes Java que representam as tabelas do banco. O Hibernate cuida de criar as tabelas automaticamente.

**Conceitos-chave antes de escrever:**
- `@Entity`: diz que essa classe é uma tabela no banco
- `@Id`: define qual campo é a chave primária
- `@GeneratedValue`: o banco gera o ID automaticamente
- `@OneToMany` / `@ManyToOne`: define o relacionamento entre Cliente e Endereço
- `@Enumerated`: usado para o campo tipoPessoa (FISICA/JURIDICA)

---

## Git — Comandos essenciais

```bash
# Ver o estado atual do repositório
git status

# Ver o histórico de commits
git log --oneline

# Adicionar arquivos para o próximo commit
git add nome-do-arquivo
# ou adicionar tudo:
git add .

# Criar um commit (snapshot do que foi feito)
git commit -m "mensagem descrevendo o que foi feito"

# Enviar para o repositório online (GitHub/GitLab)
git push origin main

# Baixar atualizações do repositório online
git pull origin main
```

**Boas práticas de commit:**
- Faça commits diariamente antes de encerrar o trabalho
- Mensagens no imperativo: "Adiciona modelo Cliente", "Cria endpoint de listagem"
- Não commite arquivos com senhas — o `.gitignore` já está configurado para ignorar o `application.properties`

---

## Como continuar no computador de casa

1. **Clone o repositório** (se ainda não tiver):
   ```bash
   git clone URL_DO_SEU_REPOSITORIO
   cd crud-project
   ```

2. **Ou, se já tiver o repositório clonado**, baixe as atualizações:
   ```bash
   git pull origin main
   ```

3. Abra o projeto no **IntelliJ IDEA**:
   - File → Open → selecione a pasta `crud-project`
   - O IntelliJ vai detectar o `pom.xml` e baixar as dependências automaticamente

4. **Continue do passo onde parou** olhando a seção "Roteiro" acima

---

## Sessões com o Claude Code

Ao abrir o Claude Code no computador de casa e navegar até a pasta do projeto, o Claude terá acesso ao contexto do projeto salvo em memória. Basta dizer:

> "Continuando o desafio de estágio — estou na Fase X, fiz Y, o que é o próximo passo?"

---

## Dúvidas frequentes

**Por que Spring Boot e não apenas Spring?**
O Spring Boot já vem configurado para funcionar com o mínimo de esforço. O Spring puro exigiria muito mais configuração manual — para um iniciante, o Boot é o caminho certo.

**O que é Maven?**
É o gerenciador de dependências do Java. O arquivo `pom.xml` lista todas as bibliotecas que o projeto usa. Quando você roda `mvn install`, ele baixa tudo automaticamente.

**O que é Hibernate?**
É um ORM (Object-Relational Mapping) — ele traduz objetos Java para tabelas no banco de dados. Em vez de escrever SQL na mão para tudo, você escreve classes Java e o Hibernate cuida do resto.

**Por que Wicket antes de Angular?**
O desafio pede explicitamente isso. Wicket é um framework mais antigo onde você escreve as telas diretamente em Java. Angular é moderno e separado do backend. Começar pelo Wicket ajuda a entender o fluxo completo antes de separar as responsabilidades.
