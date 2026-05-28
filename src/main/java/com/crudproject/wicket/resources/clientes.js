/* ============================================================
   clientes.js — scripts compartilhados pelas páginas Wicket
   (ListagemClientesPage + DetalhesClientePage e seus panels).

   Agrupa: máscaras (CPF/CNPJ/CEP/Telefone), formatadores de
   exibição, integração ViaCEP, abridores de modal com data-*,
   utilitários de UI (S/N, autoHideFeedback) e a re-inicialização
   pós-AJAX do Wicket.
   ============================================================ */

/* ───────────────── Comportamentos de máscara ───────────────── */
var maskTelefoneBehavior = function (val) {
  return val.replace(/\D/g, '').length === 11 ? '(00) 00000-0000' : '(00) 0000-00009';
};
var maskTelefoneOpcoes = {
  onKeyPress: function(val, e, field, options) {
    field.mask(maskTelefoneBehavior.apply({}, arguments), options);
  }
};
var maskCpfCnpjBehavior = function (val) {
  return val.replace(/\D/g, '').length <= 11 ? '000.000.000-009' : '00.000.000/0000-00';
};
var maskCpfCnpjOpcoes = {
  onKeyPress: function(val, e, field, options) {
    field.mask(maskCpfCnpjBehavior.apply({}, arguments), options);
  }
};

/* ───────────────── Formatadores de exibição ───────────────── */
function formatarCpfCnpj(val) {
  if (!val) return val;
  var d = val.replace(/\D/g, '');
  if (d.length === 11) return d.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4');
  if (d.length === 14) return d.replace(/(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})/, '$1.$2.$3/$4-$5');
  return val;
}
function formatarCep(val) {
  if (!val) return val;
  var d = val.replace(/\D/g, '');
  if (d.length === 8) return d.replace(/(\d{5})(\d{3})/, '$1-$2');
  return val;
}
function formatarTelefone(val) {
  if (!val) return val;
  var d = val.replace(/\D/g, '');
  if (d.length === 10) return d.replace(/(\d{2})(\d{4})(\d{4})/, '($1) $2-$3');
  if (d.length === 11) return d.replace(/(\d{2})(\d{5})(\d{4})/, '($1) $2-$3');
  return val;
}
function aplicarMascarasExibicao() {
  document.querySelectorAll('.fmt-cpf-cnpj').forEach(function(el) {
    el.textContent = formatarCpfCnpj(el.textContent.trim());
  });
  document.querySelectorAll('.fmt-cep').forEach(function(el) {
    el.textContent = formatarCep(el.textContent.trim());
  });
  document.querySelectorAll('.fmt-telefone').forEach(function(el) {
    el.textContent = formatarTelefone(el.textContent.trim());
  });
}

/* ───────────────── Integração IBGE (estados / cidades) ─────────────────
   Estratégia: <select> puro controlado por JS + <input type="hidden"
   wicket:id="..."> capturando o valor pra Wicket ler na submissão.

   - cacheEstados: lista única dos 27 UFs, baixada uma vez por sessão.
   - cacheCidades: mapa UF → lista de cidades, baixadas sob demanda.
   - ibgePopularSelectEstado(select, hidden, preselecionado): preenche um
     <select> de estados, mantém o hidden em sincronia, e seleciona o UF
     atual se informado (ex: ao reabrir o modal de editar).
   - ibgePopularSelectCidade(select, hidden, uf, preselecionada): mesma
     coisa pra cidades, dado o UF.
*/

var IBGE_BASE = 'https://servicodados.ibge.gov.br/api/v1/localidades';
var cacheEstados = null;     // Promise compartilhada
var cacheCidades = {};       // chave: UF → Promise

function ibgeListarEstados() {
  if (!cacheEstados) {
    cacheEstados = fetch(IBGE_BASE + '/estados?orderBy=nome')
      .then(function(r) { return r.json(); })
      .catch(function() { return []; });
  }
  return cacheEstados;
}

function ibgeListarCidades(uf) {
  if (!uf) return Promise.resolve([]);
  var key = uf.toUpperCase();
  if (!cacheCidades[key]) {
    cacheCidades[key] = fetch(IBGE_BASE + '/estados/' + key + '/municipios?orderBy=nome')
      .then(function(r) { return r.json(); })
      .catch(function() { return []; });
  }
  return cacheCidades[key];
}

/**
 * Preenche um <select> com a lista de estados do IBGE.
 * Mantém um <input> hidden em sincronia (para o Wicket ler).
 * Opcionalmente, dispara um callback ao mudar (útil para carregar cidades).
 */
function ibgePopularSelectEstado(selectEl, hiddenEl, preselecionado, onChange) {
  if (!selectEl) return;
  ibgeListarEstados().then(function(estados) {
    selectEl.innerHTML = '<option value="">Selecione...</option>';
    estados.forEach(function(uf) {
      var opt = document.createElement('option');
      opt.value = uf.sigla;
      opt.textContent = uf.sigla + ' — ' + uf.nome;
      if (preselecionado && uf.sigla === preselecionado) opt.selected = true;
      selectEl.appendChild(opt);
    });
    if (hiddenEl) hiddenEl.value = selectEl.value || '';

    // Atribui o listener apenas uma vez
    if (!selectEl.dataset.ibgeWired) {
      selectEl.dataset.ibgeWired = '1';
      selectEl.addEventListener('change', function() {
        if (hiddenEl) hiddenEl.value = selectEl.value;
        if (typeof onChange === 'function') onChange(selectEl.value);
      });
    }
  });
}

/**
 * Preenche um <select> com a lista de cidades do UF informado.
 * Mantém um <input> hidden em sincronia.
 */
function ibgePopularSelectCidade(selectEl, hiddenEl, uf, preselecionada) {
  if (!selectEl) return;
  if (!uf) {
    selectEl.innerHTML = '<option value="">Selecione um estado primeiro</option>';
    if (hiddenEl) hiddenEl.value = '';
    return;
  }
  ibgeListarCidades(uf).then(function(cidades) {
    selectEl.innerHTML = '<option value="">Selecione...</option>';
    cidades.forEach(function(c) {
      var opt = document.createElement('option');
      opt.value = c.nome;
      opt.textContent = c.nome;
      if (preselecionada && c.nome === preselecionada) opt.selected = true;
      selectEl.appendChild(opt);
    });
    if (hiddenEl) hiddenEl.value = selectEl.value || '';

    if (!selectEl.dataset.ibgeWired) {
      selectEl.dataset.ibgeWired = '1';
      selectEl.addEventListener('change', function() {
        if (hiddenEl) hiddenEl.value = selectEl.value;
      });
    }
  });
}

/* ───────────────── Integração ViaCEP ───────────────── */
function buscarCepNoBloco(input) {
  var cep = input.value.replace(/\D/g, '');
  if (cep.length !== 8) return;

  var bloco = input.closest('.bloco-endereco');
  if (!bloco) return;

  fetch('https://viacep.com.br/ws/' + cep + '/json/')
    .then(function(r) { return r.json(); })
    .then(function(data) {
      if (data.erro) {
        console.warn("ViaCEP: O CEP " + cep + " não foi encontrado na base dos Correios.");
        return;
      }

      var set = function(cls, val) {
        var el = bloco.querySelector('.' + cls);
        if (el && val) el.value = val;
      };

      set('viacep-logradouro', data.logradouro);
      set('viacep-bairro',     data.bairro);

      // Estado e Cidade agora são dropdowns dinâmicos do IBGE.
      // O <select> visual e o <input hidden wicket:id> ficam dentro do bloco
      // com classes .ibge-estado-select / .ibge-estado-hidden e equivalentes
      // para cidade. Após preencher o UF via ViaCEP, carregamos as cidades.
      if (data.uf) {
        var estadoSel    = bloco.querySelector('.ibge-estado-select');
        var estadoHidden = bloco.querySelector('.ibge-estado-hidden');
        if (estadoSel && estadoHidden) {
          // Seleciona o UF que veio do ViaCEP no select (já populado via init)
          for (var i = 0; i < estadoSel.options.length; i++) {
            if (estadoSel.options[i].value === data.uf) {
              estadoSel.selectedIndex = i;
              break;
            }
          }
          estadoHidden.value = data.uf;
        }
        // Carrega cidades do UF e seleciona a do ViaCEP
        var cidadeSel    = bloco.querySelector('.ibge-cidade-select');
        var cidadeHidden = bloco.querySelector('.ibge-cidade-hidden');
        if (cidadeSel && cidadeHidden) {
          ibgePopularSelectCidade(cidadeSel, cidadeHidden, data.uf, data.localidade);
        }
      }
    })
    .catch(function(e) { console.error("Erro ao consultar o ViaCEP: ", e); });
}

/* ───────────────── Abridores de modal com data-* ───────────────── */

// Abre o modal de relatório individual.
// As rotas de API REST foram removidas daqui! Agora é o Java/Wicket
// que processa o download internamente via Stream.
// Abre o modal de relatório individual passando o ID na abertura
function abrirModalRelatorio(btn) {
  var modalEl = document.getElementById('modalRelatorioLista');
  if (!modalEl) {
     console.error("ERRO: ID 'modalRelatorioLista' não encontrado no DOM.");
     return;
  }

  var id = btn.getAttribute('data-id');
  if (document.getElementById('reportClienteId')) {
     document.getElementById('reportClienteId').value = id;
  }

  new bootstrap.Modal(modalEl).show();
}

function abrirModalExclusao(id) {
  var modalEl = document.getElementById('modalConfirmarExclusao');
  if (!modalEl) {
     console.error("ERRO: ID 'modalConfirmarExclusao' não encontrado no DOM.");
     return;
  }

  var hiddenId = document.getElementById('idClienteParaExcluir');
  if (hiddenId) hiddenId.value = id;

  new bootstrap.Modal(modalEl).show();
}

function alternarLabelsCriacao(selectElement) {
  var isPJ = selectElement.value === 'JURIDICA';
  if (document.getElementById('lblCriarNome')) document.getElementById('lblCriarNome').textContent = isPJ ? 'Razão Social' : 'Nome completo';
  if (document.getElementById('lblCriarCpfCnpj')) document.getElementById('lblCriarCpfCnpj').textContent = isPJ ? 'CNPJ' : 'CPF';
  if (document.getElementById('lblCriarRgIe')) document.getElementById('lblCriarRgIe').textContent = isPJ ? 'Inscrição Estadual' : 'RG';
  if (document.getElementById('lblCriarData')) document.getElementById('lblCriarData').textContent = isPJ ? 'Data de Fundação' : 'Data de Nascimento';
}

function abrirModalEdicao(btn) {
  var modalEl = document.getElementById('modalEditarCliente');
  if (!modalEl) {
     console.error("ERRO: ID 'modalEditarCliente' não encontrado no DOM.");
     return;
  }

  var isPJ = btn.dataset.tipo === 'JURIDICA';

  if (document.getElementById('editNome')) document.getElementById('editNome').value = btn.dataset.nome || '';
  if (document.getElementById('editCpfCnpj')) document.getElementById('editCpfCnpj').value = formatarCpfCnpj(btn.dataset.cpfCnpj || '');

  if (document.getElementById('editClienteId')) document.getElementById('editClienteId').value = btn.dataset.id;
  if (document.getElementById('editEmail')) document.getElementById('editEmail').value = btn.dataset.email || '';
  if (document.getElementById('editRgIe')) document.getElementById('editRgIe').value = btn.dataset.rgIe || '';
  if (document.getElementById('editAtivo')) document.getElementById('editAtivo').checked = (btn.dataset.ativo === 'true');

  if (document.getElementById('editLabelNome')) document.getElementById('editLabelNome').textContent = isPJ ? 'Razão Social' : 'Nome completo';
  if (document.getElementById('editLabelCpfCnpj')) document.getElementById('editLabelCpfCnpj').textContent = isPJ ? 'CNPJ' : 'CPF';
  if (document.getElementById('editCampoIeContainer')) document.getElementById('editCampoIeContainer').style.display = isPJ ? '' : 'none';

  new bootstrap.Modal(modalEl).show();
}

function abrirModalExcluirEndereco(btn) {
  if (document.getElementById('idEnderecoParaExcluir')) document.getElementById('idEnderecoParaExcluir').value = btn.dataset.id;
  if (document.getElementById('txtEnderecoParaExcluir')) document.getElementById('txtEnderecoParaExcluir').textContent = btn.dataset.logradouro || '';

  var modalEl = document.getElementById('modalConfirmarExcluirEndereco');
  if (modalEl) new bootstrap.Modal(modalEl).show();
}

function abrirModalEditarEndereco(btn) {
  // Helper local — não polui o escopo global.
  var set = function(id, val) {
    var el = document.getElementById(id);
    if (el) el.value = val || '';
  };

  // Preenche os campos de texto a partir dos data-* attributes.
  // Estado e cidade são tratados via IBGE abaixo (não como campos de texto).
  set('editEnderecoId',    btn.dataset.id);
  set('editEndLogradouro', btn.dataset.logradouro);
  set('editEndNumero',     btn.dataset.numero);
  set('editEndComplemento', btn.dataset.complemento);
  set('editEndBairro',     btn.dataset.bairro);
  set('editEndCep',        formatarCep(btn.dataset.cep));
  set('editEndPais',       btn.dataset.pais);
  // Hidden inputs (Wicket lê pra binding) — selects ficam em sincronia
  set('editEndEstado',     btn.dataset.estado);
  set('editEndCidade',     btn.dataset.cidade);

  if (document.getElementById('editEndPrincipal')) {
    document.getElementById('editEndPrincipal').checked = btn.dataset.principal === 'true';
  }

  if (document.getElementById('editEndTelefone')) {
    $('#editEndTelefone').val(btn.dataset.telefone || '')
      .mask(maskTelefoneBehavior, maskTelefoneOpcoes);
  }

  var modalEl = document.getElementById('modalEditarEndereco');
  if (modalEl) {
    // Inicializa os dropdowns IBGE pré-selecionando o UF e a cidade do endereço
    inicializarIbgeNoElemento(modalEl, btn.dataset.estado, btn.dataset.cidade);
    new bootstrap.Modal(modalEl).show();
  }
}

/**
 * Consulta ViaCEP no modal de editar endereço.
 * Diferente do modal de criar (que tem múltiplos endereços em .bloco-endereco),
 * aqui há um único endereço com IDs fixos. Os dropdowns de estado e cidade
 * são preenchidos via IBGE.
 */
function buscarCepEditarEndereco(input) {
  var cep = input.value.replace(/\D/g, '');
  if (cep.length !== 8) return;

  fetch('https://viacep.com.br/ws/' + cep + '/json/')
    .then(function(r) { return r.json(); })
    .then(function(data) {
      if (data.erro) {
        console.warn("ViaCEP: O CEP " + cep + " não foi encontrado.");
        return;
      }
      var set = function(id, val) {
        var el = document.getElementById(id);
        if (el && val) el.value = val;
      };
      set('editEndLogradouro', data.logradouro);
      set('editEndBairro',     data.bairro);

      // Estado e Cidade agora são dropdowns dinâmicos
      if (data.uf) {
        var estadoSel    = document.getElementById('editEndEstadoSelect');
        var estadoHidden = document.getElementById('editEndEstado');
        if (estadoSel && estadoHidden) {
          for (var i = 0; i < estadoSel.options.length; i++) {
            if (estadoSel.options[i].value === data.uf) {
              estadoSel.selectedIndex = i;
              break;
            }
          }
          estadoHidden.value = data.uf;
        }
        var cidadeSel    = document.getElementById('editEndCidadeSelect');
        var cidadeHidden = document.getElementById('editEndCidade');
        if (cidadeSel && cidadeHidden) {
          ibgePopularSelectCidade(cidadeSel, cidadeHidden, data.uf, data.localidade);
        }
      }
    })
    .catch(function(e) { console.error("Erro ao consultar o ViaCEP: ", e); });
}

/**
 * Inicializa os dropdowns IBGE de um modal. Deve ser chamado quando o
 * modal abre e quando o Wicket re-renderiza componentes via AJAX.
 *
 * - `seletorRaiz`: elemento DOM que contém os selects (modal ou bloco-endereco)
 * - `ufAtual`, `cidadeAtual`: valores pré-selecionados (no editar, vêm do data-*)
 */
function inicializarIbgeNoElemento(raiz, ufAtual, cidadeAtual) {
  if (!raiz) return;
  var estadoSel    = raiz.querySelector('.ibge-estado-select');
  var estadoHidden = raiz.querySelector('.ibge-estado-hidden');
  var cidadeSel    = raiz.querySelector('.ibge-cidade-select');
  var cidadeHidden = raiz.querySelector('.ibge-cidade-hidden');

  if (estadoSel && estadoHidden) {
    ibgePopularSelectEstado(estadoSel, estadoHidden, ufAtual, function(uf) {
      // Quando o usuário muda o estado manualmente, recarrega cidades e limpa a anterior
      ibgePopularSelectCidade(cidadeSel, cidadeHidden, uf, null);
    });
  }
  if (cidadeSel && cidadeHidden && ufAtual) {
    ibgePopularSelectCidade(cidadeSel, cidadeHidden, ufAtual, cidadeAtual);
  }
}

function mostrarAvisoNaoPodeDeletar(btn) {
  if (document.getElementById('txtAvisoEnderecoNaoPodeDeletar')) {
    document.getElementById('txtAvisoEnderecoNaoPodeDeletar').textContent = btn.dataset.motivo || 'Não é possível excluir este endereço.';
  }
  var modalEl = document.getElementById('modalAvisoEnderecoNaoPodeDeletar');
  if (modalEl) new bootstrap.Modal(modalEl).show();
}

/* ───────────────── Utilitários de UI ───────────────── */
function toggleSemNumero(btn) {
  var input = btn.closest('.input-group').querySelector('input');
  if (!input) return;

  if (input.readOnly) {
    input.readOnly = false;
    input.value = '';
    btn.classList.remove('btn-secondary');
    btn.classList.add('btn-outline-secondary');
  } else {
    input.value = 'SN';
    input.readOnly = true;
    btn.classList.remove('btn-outline-secondary');
    btn.classList.add('btn-secondary');
  }
}

/* ───────────────── Bootstrap das máscaras + autoHide ───────────────── */
function initMasks() {
  $('.mask-cep').mask('00000-000');
  $('.mask-telefone').mask(maskTelefoneBehavior, maskTelefoneOpcoes);
  $('.mask-cpf-cnpj').mask(maskCpfCnpjBehavior, maskCpfCnpjOpcoes);
  $('#criarDataMask').mask('00/00/0000');
  aplicarMascarasExibicao();
  initIbgeSelects();
}

/**
 * Inicializa todos os selects IBGE que ainda não foram populados.
 * Cobre:
 *   - Blocos-endereço dinâmicos do modal de criar (novos adicionados via AJAX)
 *   - Modal de adicionar endereço (tem 1 bloco fixo)
 * O modal de editar é inicializado em abrirModalEditarEndereco (pré-seleciona valores).
 */
function initIbgeSelects() {
  document.querySelectorAll('.bloco-endereco').forEach(function(bloco) {
    var estadoSel = bloco.querySelector('.ibge-estado-select');
    // Já populado? Pula. (data.ibgeWired é setado dentro de ibgePopularSelectEstado)
    if (estadoSel && !estadoSel.dataset.ibgeWired) {
      var ufAtual    = (bloco.querySelector('.ibge-estado-hidden') || {}).value || null;
      var cidadeAtual = (bloco.querySelector('.ibge-cidade-hidden') || {}).value || null;
      inicializarIbgeNoElemento(bloco, ufAtual, cidadeAtual);
    }
  });
}

function autoHideFeedback() {
  setTimeout(function() {
    $('.feedbackPanel').fadeOut('slow', function() {
      $(this).empty();
      $(this).css('display', '');
    });
  }, 5000);
}

function waitFor(check, callback, maxTentativas) {
  var t = 0, limite = maxTentativas || 200;
  (function tick() {
    if (check()) { callback(); return; }
    if (t++ > limite) return;
    setTimeout(tick, 50);
  })();
}

waitFor(
  function () { return window.jQuery && jQuery.fn && jQuery.fn.mask && window.bootstrap; },
  function () { initMasks(); autoHideFeedback(); }
);

waitFor(
  function () { return typeof Wicket !== 'undefined' && Wicket.Event && Wicket.Event.subscribe; },
  function () {
    Wicket.Event.subscribe('/ajax/call/complete', function (jqEvent, attributes, jqXHR, errorThrown, textStatus) {
      initMasks();
      autoHideFeedback();
    });
  }
);