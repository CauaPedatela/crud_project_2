# resources/ — Configuração e Templates de Relatórios

> Leia também o `CLAUDE.md` da raiz antes de qualquer alteração aqui.

## Responsabilidade
- **`application.properties`** — configuração local do Spring Boot (DB, porta, JPA).
- **`application.properties.example`** — versão de exemplo, **rastreada no git** (sem credenciais reais).
- **`reports/cliente-detalhes.jrxml`** — template Jasper para relatório de detalhes de cliente.
- **`reports/clientes-lista.jrxml`** — template Jasper para lista de clientes.

## ⚠️ Regra forte: secrets fora do git

- **`application.properties`** está (ou deveria estar) no `.gitignore` — ele tem senha do MySQL.
- Mudanças de configuração de produção/usuário vão em `application.properties.example` (no git) **e** o usuário replica localmente no `application.properties`.
- **Nunca commitar credenciais.** Se notar credencial real no diff antes de commit, **avisar o usuário e parar**.

## ⚠️ Templates Jasper (.jrxml)

- Editados visualmente no **Jasper Studio 6.11** (instalação obrigatória pelo PDF seção 1).
- Edição direta no XML é aceitável apenas para:
  - Ajuste de caminho de subreport.
  - Mudança de `fontSize`, cor, label literal pequena.
  - Correção de typo.
- **Mudanças estruturais** (adicionar campo, mudar layout, agrupar) → fazer no Studio, não no XML cru.
- Conferir que o `.jrxml` continua válido após edição (Spring Boot falha no startup se Jasper não compilar).

## Pode alterar sem perguntar
- Adicionar `.properties` novos auxiliares (mensagens, i18n).
- Ajustar `application.properties.example` para refletir nova config (sem credenciais).
- Editar `.jrxml` para correções pequenas listadas acima.

## ⛔ EXIGE confirmação antes de alterar
- **`application.properties` (real)** — pode quebrar o ambiente local do usuário.
- Mudar string de conexão JDBC, porta do servidor, `spring.jpa.hibernate.ddl-auto`.
- Mudar layout estrutural de relatórios Jasper sem usar o Studio.
- Adicionar/remover templates `.jrxml`.
- Mover arquivos para fora desta pasta.

## Convenções
- Comentários no `.properties` em pt-BR, com `#`.
- Nomes de templates Jasper em kebab-case (`cliente-detalhes.jrxml`).
