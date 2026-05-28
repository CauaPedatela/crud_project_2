# wicket/page/ — Páginas e Panels do Wicket

> Leia também o `CLAUDE.md` da raiz antes de qualquer alteração aqui.
> Este CLAUDE.md cobre também os subdiretórios `listagem/`, `detalhes/`, `shared/`, e o estado em `../state/`.

## Responsabilidade
Frontend Wicket. Cada componente é um par `.java` + `.html` no mesmo pacote.

**Fluxo de dados:** `Page → @SpringBean → ClienteService → Repository → MySQL`
Páginas Wicket conectam **direto ao Service** — **não passam pelo Controller REST**.

## ⚠️ Regras invioláveis do Wicket

### 1) Cada `.java` precisa de um `.html` no mesmo pacote
- IDs dos componentes Java e do HTML têm que bater (`wicket:id="x"` ↔ `add(new Foo("x"))`).
- HTML começa com `<html xmlns:wicket="http://wicket.apache.org/dtds.data/wicket-xhtml1.4-strict.dtd">`.
- O `pom.xml` copia HTMLs de `src/main/java` (bloco `<resources>`) — não mover HTML pra `src/main/resources`.

### 2) `setOutputMarkupId(true)` é OBRIGATÓRIO em componentes AJAX-alvo
Qualquer componente que sofrer `target.add(componente)` precisa disso. Sem isso, o AJAX silenciosamente não atualiza nada.

```java
WebMarkupContainer container = new WebMarkupContainer("foo");
container.setOutputMarkupId(true);  // ← obrigatório
```

### 3) Modelos sempre serializáveis
Usar `LoadableDetachableModel`, `IModel`, `CompoundPropertyModel`. **Nunca segurar entidade JPA viva em campo da página** — fica detached e quebra serialização.

### 4) Form dentro de Modal Bootstrap
A estrutura correta é `<div class="modal">` envolvendo `<form wicket:id>` — não o contrário. CSS necessário no `<style>` da página:

```css
form { display: contents; }  /* preserva scroll do .modal-body sem quebrar submit */
```

### 5) FeedbackPanel em dois níveis
- **Page-level** (`#feedback` no topo da página) — para sucesso/erro globais (excluir, editar).
- **Modal-scoped** (`#feedbackCriar` dentro do modal) com `new ComponentFeedbackMessageFilter(form)` — erros de criação aparecem dentro do modal, sem fechá-lo.

CSS dos painéis já está nas páginas com cara de Bootstrap alert (`.feedbackPanelERROR`/`INFO`/`WARNING`).

### 6) Componentes exigidos pelo PDF (seção 8.1)
Usar pelo nome quando aplicável: `Panel`, `TextField`, `NumberTextField`, `RadioGroup`/`Radio`, `CheckBox`, `ListView`/`PageableListView`, `Link`, `Submit`/`AjaxButton`, `AjaxRequestTarget`.

### 7) Datas no frontend
- Máscara `dd/mm/aaaa` via **jQuery Mask Plugin**.
- Parse no Java via `DateTimeFormatter.ofPattern("dd/MM/yyyy")`.
- **Não misturar formatos** — gera ParseException silenciosa.

### 8) Dropdowns dinâmicos (IBGE) — padrão hidden field + select JS

`DropDownChoice` do Wicket valida a submissão contra uma **lista estática**.
Para listas dinâmicas (UF/Cidade vindas da API do IBGE) usamos um padrão diferente:

```html
<!-- <select> puro HTML (sem wicket:id), populado pelo JS -->
<select class="ibge-estado-select">
  <option value="">Carregando…</option>
</select>
<!-- HiddenField com wicket:id — o JS mantém em sincronia, Wicket lê na submissão -->
<input type="hidden" class="ibge-estado-hidden" wicket:id="endEstado">
```

```java
// No .java: HiddenField, não DropDownChoice
form.add(new HiddenField<>("endEstado", new PropertyModel<>(state, "estado")));
```

O JS (`clientes.js`) cuida do resto: `ibgePopularSelectEstado`/`ibgePopularSelectCidade`
populam as opções, e `inicializarIbgeNoElemento` é chamado quando o modal abre.
A inicialização global acontece em `initMasks()` → `initIbgeSelects()`.

> **Por que não DropDownChoice com lista dinâmica?** Porque o Wicket valida o
> valor submetido contra a lista que conhece. Como JS pode adicionar opções
> que o Wicket não viu, a validação falha. HiddenField não tem esse problema.

## Pode alterar sem perguntar
- Adicionar/refatorar panels internos dentro do mesmo pacote.
- Ajustar CSS / estilos.
- Melhorar UX, adicionar tooltips, ajustar labels.
- Adicionar novos botões/modais que sigam os padrões acima.

## ⛔ EXIGE confirmação antes de alterar
- **Mudar a rota da homepage** (`WicketApplication.getHomePage()`).
- **Mexer em `WicketApplication` ou `WicketConfig`** (`../WicketApplication.java`, `../WicketConfig.java`).
- Substituir `PageableListView` por outro mecanismo de paginação.
- Alterar o `FiltroState` (estado compartilhado entre `BuscaPanel`, `FiltrosPanel`, `TabelaClientesPanel`).
- Trocar Bootstrap por outro framework CSS.
- Remover páginas inteiras ou renomear pacotes de páginas.

## Convenções
- Comentário em pt-BR em cada método de página/panel explicando o quê e o porquê.
- Não duplicar lógica de validação aqui — chamar Service e tratar exceção.
- `target.add(...)` só nos componentes que **realmente** mudaram visualmente.
