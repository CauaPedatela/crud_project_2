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
  ├── Controller → recebe as requisições HTTP
  ├── DTO        → contrato de entrada/saída (JSON)
  ├── Mapper     → converte DTO ↔ Entity
  ├── Service    → aplica as regras de negócio
  ├── Repository → acessa o banco (Spring Data JPA)
  └── DAO        → queries avançadas (HQL, SQL puro, Search) — Fase futura
        ↕  Hibernate (JPA)
BANCO DE DADOS (MySQL via Docker)
```

**Padrão MVC:**
- **M** (Model): classes Java que representam os dados (`Cliente`, `Endereco`)
- **V** (View): telas do usuário (Wicket ou Angular)
- **C** (Controller): código que recebe as requisições e decide o que fazer

---

## Estado atual do projeto

> Última atualização: **CRUD completo de Cliente/Endereço em REST. Banco zerado, pronto para refactor grande baseado no modelo passado pelo supervisor.**

### ✅ Concluído

**Infraestrutura**
- Java 17 + Maven + IntelliJ
- `pom.xml` com Spring Boot 2.7, Hibernate 5, MySQL driver, JasperReports, Apache POI
- **MySQL via Docker Compose** (`docker-compose.yml` na raiz)
- `application.properties` com mensagens de erro detalhadas (no `.gitignore`, com `.example` no repo)

**Models (entidades JPA)**
- `Cliente.java` — PF + PJ na mesma tabela, com `@JsonManagedReference`
- `Endereco.java` — FK para Cliente, com `@JsonBackReference`
- `TipoPessoa.java` — enum FISICA / JURIDICA

**Repositories**
- `ClienteRepository` — `findByCpf`, `findByCnpj`
- `EnderecoRepository` — `findByClienteId`

**DTOs** (sufixados por intenção — contratos claros)
- `dto/cliente/`
  - `ClienteCadastroDTO` — POST (com lista de endereços iniciais embedded)
  - `ClienteAtualizacaoDTO` — PUT (sem tipoPessoa, sem enderecos)
  - `ClienteResponseDTO` — saída (com lista de endereços)
- `dto/endereco/`
  - `EnderecoCadastroDTO` — POST (com clienteId)
  - `EnderecoAtualizacaoDTO` — PUT (sem clienteId)
  - `EnderecoResponseDTO` — saída

**Mappers**
- `ClienteMapper` — toEntity, toResponse, updateEntity, lida com endereços embedded
- `EnderecoMapper` — toEntity (recebe Cliente do Service), toResponse, updateEntity

**Services**
- `ClienteService` — `salvar`, `buscarTodos`, `buscarPorId`, `atualizar`, `excluir`
  - Validação cruzada PF/PJ
  - Algoritmo Receita Federal pra CPF/CNPJ
  - Sincronização de endereços no salvar (mín. 1, ajusta principal)
  - tipoPessoa imutável após cadastro
- `EnderecoService` — CRUD completo + `definirComoPrincipal`
  - Regras de endereço principal automatizadas
  - Bloqueio de mover endereço entre clientes

**Controllers REST**
- `ClienteController` — `POST`, `GET`, `GET/{id}`, `PUT/{id}`, `DELETE/{id}`
- `EnderecoController` — `POST`, `GET/{id}`, `GET cliente/{clienteId}`, `PUT/{id}`, `DELETE/{id}`, `PUT/{id}/principal`

**Banco de dados**
- Banco `crud_project_db` rodando no Docker
- ⚠ **Tabelas `tb_cliente` e `tb_endereco` foram dropadas em preparação para o refactor**

---

## Próximo passo — REFACTOR GRANDE

> Quando voltar ao projeto, retome aqui.

O supervisor passou um novo modelo de dados que exige refatoração. O JSON novo:

```json
{
  "id": 1001,
  "tipoPessoa": "FISICA",
  "nome": "João Carlos da Silva",
  "cpfCnpj": "12345678901",
  "rgInscricaoEstadual": "123456789",
  "dataNascimento": "1990-05-15",
  "email": "joao.silva@email.com",
  "telefone": "(11) 99999-9999",
  "ativo": true,
  "dataCadastro": "2026-05-11T10:30:00",
  "enderecos": [
    {
      "id": 1,
      "tipo": "RESIDENCIAL",
      "logradouro": "Rua das Flores",
      "numero": "123",
      "complemento": "Apartamento 45",
      "bairro": "Centro",
      "cidade": "São Paulo",
      "estado": "SP",
      "cep": "01001-000",
      "pais": "Brasil",
      "principal": true
    }
  ]
}
```

### Decisões já tomadas

| Pergunta | Decisão |
|---|---|
| Wrapper `{ "cliente": {...} }`? | **NÃO** — JSON direto na raiz, padrão REST |
| `dataNascimento` para PJ? | Sim — mesmo campo, semântica de "data de fundação" |
| PUT com endereços: replace ou sync? | **Sync** — id existe → atualiza, sem id → cria, sumiu → deleta |
| Manter rotas de Endereço separadas? | **NÃO** — tudo passa pelo `/api/clientes` |

### Mudanças que serão feitas

**Model — `Cliente.java`**
- Remove: `cpf`, `rg`, `cnpj`, `razaoSocial`, `inscricaoEstadual`, `dataCriacao`
- Adiciona: `cpfCnpj`, `rgInscricaoEstadual`, `telefone`, `dataCadastro`
- Mantém: `id`, `tipoPessoa`, `nome`, `dataNascimento`, `email`, `ativo`, `enderecos`

**Model — `Endereco.java`**
- Remove: `telefone` (vai pra Cliente)
- Renomeia: `enderecoPrincipal` → `principal`
- Adiciona: `tipo` (enum `TipoEndereco`), `pais`

**Model — novo enum `TipoEndereco.java`**
- `RESIDENCIAL, COMERCIAL`

**DTOs — unificação**
- `ClienteCadastroDTO` + `ClienteAtualizacaoDTO` → `ClienteDTO` (único)
- `EnderecoCadastroDTO` + `EnderecoAtualizacaoDTO` → `EnderecoDTO` (com `id` opcional)
- `ClienteResponseDTO` e `EnderecoResponseDTO` mantidos, com os novos campos

**Service**
- `EnderecoService.java` → **DELETADO**
- `ClienteService` ganha:
  - Validação `cpfCnpj` (length 11 = PF, 14 = PJ)
  - Método `sincronizarEnderecos` (semântica de sync)
  - `dataCadastro = now()` no salvar

**Controller**
- `EnderecoController.java` → **DELETADO**
- `ClienteController` mantém as 5 rotas, com `ClienteDTO` unificado

**Repository**
- `ClienteRepository.findByCpfCnpj(String)` substitui `findByCpf` + `findByCnpj`

### Plano de execução amanhã

1. Pull do projeto
2. Levantar Docker (`docker compose up -d`)
3. Subir Spring Boot uma vez para o Hibernate criar tabelas novas
4. Iniciar o refactor: model → DTO → mapper → service → controller
5. Testar todas as rotas no Apidog

---

## Fluxo do cadastro — do Apidog ao banco

Quando uma rota REST é chamada (ex: pelo Apidog, Angular ou Postman), o dado atravessa todas as camadas.

```
APIDOG (cliente HTTP)
       │ envia JSON via HTTP
       ▼
SPRING (recebe a requisição)
       │ converte JSON em DTO
       ▼
CONTROLLER (ClienteController)
       │ repassa o DTO para o Service
       ▼
SERVICE (ClienteService)
       │ ① mapeia DTO em entidade
       │ ② aplica regras de negócio
       │ ③ manda Repository salvar
       ▼
MAPPER (ClienteMapper)
       │ copia DTO → Cliente
       ▼
REPOSITORY (ClienteRepository)
       │ executa INSERT/UPDATE
       ▼
MYSQL (Docker)
       │ gera id, devolve ao Hibernate
       ▼
SERVICE (caminho de volta)
       │ mapeia Cliente → DTO de saída
       ▼
CONTROLLER → SPRING
       │ converte DTO em JSON
       ▼
APIDOG (resposta)
```

**Resumo em uma frase:** o DTO atravessa Controller e Service, vira Entity no Mapper, é salvo pelo Repository, e volta o caminho inverso até o JSON que o Apidog recebe.

A entidade nunca é exposta — quem sai e entra é sempre DTO.

### Por que cada camada existe

| Camada | Por que precisa existir |
|---|---|
| **Controller** | Tradutor HTTP ↔ Java |
| **DTO** | Embalagem segura, não expõe a entidade |
| **Service** | Lugar das regras de negócio |
| **Mapper** | Centraliza a conversão entre DTO e Entity |
| **Repository** | Fala com o banco |
| **Entity** | Mapeia a tabela do banco |

### E o Wicket?

O Wicket **não usa HTTP entre frontend e backend** — ele roda no mesmo servidor que o Spring Boot. A página Wicket chama o `Service` **diretamente em Java**, sem passar por Controller nem JSON.

```
WICKET                        APIDOG / ANGULAR
──────                        ──────────────
ClientePage.java              Apidog envia JSON
   │                                │
   │ chama em Java                  │ HTTP POST
   ▼                                ▼
ClienteService.salvar(dto)  ◄── ClienteController
   │
   ▼
ClienteRepository.save(entity)
   │
   ▼
MySQL
```

**A camada Service é o ponto de encontro.** Tudo abaixo dela é reaproveitado pelos dois frontends.

---

## Tecnologias

| Tecnologia | Para que serve |
|---|---|
| Java 17 | Linguagem do backend |
| Spring Boot 2.7 | Framework Web + DI + auto-config |
| Hibernate 5 | ORM (objeto Java → tabela) |
| MySQL 8 (Docker) | Banco de dados |
| Apache Wicket 7 | Framework para criar telas em Java (Fase 1) |
| Angular 14 | Framework frontend moderno (Fase 2) |
| JasperReports | Relatórios PDF |
| Apache POI | Planilhas Excel |
| JUnit | Testes automatizados |
| Apidog | Cliente HTTP para testes |

> **Observação:** Spring Boot 2.7 (não 3.x) por compatibilidade com Wicket 7. O `wicket-spring-boot-starter` foi **removido** por incompatibilidade — será feita integração manual quando chegar a fase do Wicket.

---

## Modelos de dados — após refactor (próximo passo)

### Cliente

| Campo | Tipo | Observação |
|---|---|---|
| id | Long | PK |
| tipoPessoa | Enum | FISICA / JURIDICA — imutável após cadastro |
| nome | String | Nome (PF) ou Razão Social (PJ) |
| cpfCnpj | String | CPF (11 dígitos) ou CNPJ (14 dígitos) |
| rgInscricaoEstadual | String | RG (PF) ou Inscrição Estadual (PJ) |
| dataNascimento | LocalDate | Nascimento (PF) ou Fundação (PJ) |
| email | String | Email do cliente |
| telefone | String | Telefone principal |
| ativo | Boolean | Status |
| dataCadastro | LocalDateTime | Gerado no save automaticamente |
| enderecos | List\<Endereco\> | Relação 1:N |

> **Decisão arquitetural:** uma única tabela `tb_cliente`. Campos são unificados (não há separação cpf/cnpj). As validações de negócio (CPF vs CNPJ) acontecem no Service baseado no `tipoPessoa`.

### Endereço

| Campo | Tipo | Observação |
|---|---|---|
| id | Long | PK |
| tipo | Enum | RESIDENCIAL / COMERCIAL |
| logradouro | String | |
| numero | String | |
| complemento | String | Opcional |
| bairro | String | |
| cidade | String | |
| estado | String | UF |
| cep | String | |
| pais | String | |
| principal | Boolean | Apenas 1 principal por cliente |

---

## Estrutura de pastas atual

```
src/main/java/com/crudproject/
├── CrudProjectApplication.java     ← entrypoint
├── model/
│   ├── Cliente.java
│   ├── Endereco.java
│   └── TipoPessoa.java
├── dto/
│   ├── cliente/
│   │   ├── ClienteCadastroDTO.java
│   │   ├── ClienteAtualizacaoDTO.java
│   │   └── ClienteResponseDTO.java
│   └── endereco/
│       ├── EnderecoCadastroDTO.java
│       ├── EnderecoAtualizacaoDTO.java
│       └── EnderecoResponseDTO.java
├── mapper/
│   ├── ClienteMapper.java
│   └── EnderecoMapper.java
├── repository/
│   ├── ClienteRepository.java
│   └── EnderecoRepository.java
├── service/
│   ├── ClienteService.java
│   └── EnderecoService.java
├── controller/
│   ├── ClienteController.java
│   └── EnderecoController.java
└── wicket/page/                    ← vazio (Fase 1)

docker-compose.yml                  ← MySQL 8 em container
```

---

## Configuração local

### MySQL via Docker (recomendado)

O projeto roda o MySQL em container Docker. **Não precisa instalar MySQL nativamente**.

```bash
# Subir o banco
docker compose up -d

# Verificar status
docker compose ps    # deve mostrar "healthy"

# Desligar (mantém os dados)
docker compose down

# ⚠ NUNCA use down -v — apaga todos os dados
```

A conexão JDBC continua sendo `localhost:3306` — indistinguível de um MySQL nativo.

### application.properties

Está no `.gitignore` pra proteger a senha. Existe um `application.properties.example` no repo como modelo.

**Ao clonar em outro PC:**
1. Copie `src/main/resources/application.properties.example` para `application.properties`
2. Substitua `SUA_SENHA_AQUI` por `UnikaDevDB` (ou a senha que escolheu pro container)
3. Suba o Docker: `docker compose up -d`

---

## Git — Comandos essenciais

```bash
git status                       # estado atual
git log --oneline                # histórico
git add .                        # adicionar tudo
git commit -m "mensagem"         # commit
git push origin main             # enviar pro GitHub
git pull origin main             # baixar do GitHub
git reset --hard origin/main     # voltar pro último push (descarta locais)
git clean -fd                    # remove arquivos não-rastreados
```

**Boas práticas:**
- Commits diários
- Prefixos: `feat:`, `chore:`, `docs:`, `refactor:`, `fix:`
- Nunca commitar senhas — `.gitignore` cuida

---

## Como continuar amanhã

1. **Pull do repositório:**
   ```bash
   git pull origin main
   ```

2. **Subir o Docker:**
   ```bash
   docker compose up -d
   ```

3. **Abrir o IntelliJ** e configurar `application.properties` (se for um PC novo)

4. **Continuar do refactor** — ler a seção "Próximo passo — REFACTOR GRANDE" acima

---

## Sessões com o Claude Code

Ao abrir o Claude Code amanhã, basta dizer:

> "Continuando o desafio — leia o README e vamos fazer o refactor do modelo do supervisor."

O Claude vai ler o estado atual e o plano, e retoma de onde foi deixado.

---

## Dúvidas frequentes

**Por que Spring Boot 2.7 e não 3.x?**
Compatibilidade com Wicket 7 (que o desafio pede). Spring Boot 3 exigiria Wicket 9+.

**Por que removi o `wicket-spring-boot-starter`?**
A versão 3.x do starter quebrou com Wicket 7 (`NoSuchMethodError` no setTimeout). Quando chegar a fase do Wicket, vamos integrar manualmente com uma `@Configuration` que registra o `WicketFilter`.

**Por que DTOs se a Entity já existe?**
Para não expor a entidade JPA diretamente — segurança e desacoplamento. DTOs são "embalagens" sem anotações JPA, sem ciclos de serialização e sem campos sensíveis.

**Por que usar Docker pro MySQL?**
Portabilidade. Não precisa instalar MySQL em cada máquina onde for desenvolver — basta ter Docker instalado e rodar `docker compose up -d`.

**Repository vs DAO?**
Repository (Spring Data JPA) cobre CRUD simples. DAO entra para queries avançadas exigidas pelo desafio: Search dinâmico (`com.googlecode.genericdao.search`), HQL, SQL puro.
