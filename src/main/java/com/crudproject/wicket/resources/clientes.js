/* ============================================================
   clientes.js — scripts compartilhados pelas páginas Wicket
   (ListagemClientesPage + DetalhesClientePage e seus panels).

   Agrupa: máscaras (CPF/CNPJ/CEP/Telefone), formatadores de
   exibição, integração ViaCEP, abridores de modal com data-*,
   utilitários de UI (S/N, autoHideFeedback) e a re-inicialização
   pós-AJAX do Wicket.
   ============================================================ */

/* ───────────────── Comportamentos de máscara ─────────────────
   Declarados em escopo global pra serem reutilizados nos handlers
   de abertura de modal (re-aplicar máscara em valores preenchidos
   programaticamente via .value = ...). */
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

/* ───────────────── Integração ViaCEP ─────────────────
   Ao sair do campo CEP com 8 dígitos, busca o endereço e
   preenche os campos do mesmo bloco via classes .viacep-*. */
function buscarCepNoBloco(input) {
  var cep = input.value.replace(/\D/g, '');
  if (cep.length !== 8) return;
  var bloco = input.closest('.border.rounded') || input.closest('.modal-body');
  if (!bloco) return;
  fetch('https://viacep.com.br/ws/' + cep + '/json/')
    .then(function(r) { return r.json(); })
    .then(function(data) {
      if (data.erro) return;
      var set = function(cls, val) {
        var el = bloco.querySelector('.' + cls);
        if (el && val) el.value = val;
      };
      set('viacep-logradouro', data.logradouro);
      set('viacep-bairro',     data.bairro);
      set('viacep-cidade',     data.localidade);
      // O Wicket usa índice como value da <option>, não o texto da UF.
      // Itera as options e seleciona pelo texto (ex: "SP").
      if (data.uf) {
        var sel = bloco.querySelector('.viacep-estado');
        if (sel) {
          for (var i = 0; i < sel.options.length; i++) {
            if (sel.options[i].text === data.uf) { sel.selectedIndex = i; break; }
          }
        }
      }
    })
    .catch(function() {});
}

/* ───────────────── Abridores de modal com data-* ───────────────── */

// Abre o modal de relatório individual e ajusta os links de PDF/Excel.
function abrirModalRelatorio(id) {
  var modalEl = document.getElementById('modalRelatorioLista');
  if (!modalEl) {
     console.error("ERRO: ID 'modalRelatorioLista' não encontrado no DOM.");
     return;
  }

  var linkPdf = document.getElementById('linkRelatorioPdfLista');
  if (linkPdf) linkPdf.href = '/api/relatorios/cliente/detalhes/pdf?id=' + id;

  var linkExcel = document.getElementById('linkRelatorioExcelLista');
  if (linkExcel) linkExcel.href = '/api/relatorios/cliente/detalhes/excel?id=' + id;

  new bootstrap.Modal(modalEl).show();
}

// Abre o modal de confirmação de exclusão de cliente.
// Nota: O Wicket passa apenas o ID diretamente para esta função.
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

// Troca os rótulos PF/PJ no modal de criação.
function alternarLabelsCriacao(selectElement) {
  var isPJ = selectElement.value === 'JURIDICA';
  if (document.getElementById('lblCriarNome')) document.getElementById('lblCriarNome').textContent = isPJ ? 'Razão Social' : 'Nome completo';
  if (document.getElementById('lblCriarCpfCnpj')) document.getElementById('lblCriarCpfCnpj').textContent = isPJ ? 'CNPJ' : 'CPF';
  if (document.getElementById('lblCriarRgIe')) document.getElementById('lblCriarRgIe').textContent = isPJ ? 'Inscrição Estadual' : 'RG';
  if (document.getElementById('lblCriarData')) document.getElementById('lblCriarData').textContent = isPJ ? 'Data de Fundação' : 'Data de Nascimento';
}

// Pré-preenche o modal de edição de cliente com os data-* do botão clicado.
function abrirModalEdicao(btn) {
  var modalEl = document.getElementById('modalEditarCliente');
  if (!modalEl) {
     console.error("ERRO: ID 'modalEditarCliente' não encontrado no DOM.");
     return;
  }

  var isPJ = btn.dataset.tipo === 'JURIDICA';

  // Preenche valores apenas se os elementos existirem na tela
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

// Abre o modal de confirmação de exclusão de endereço.
function abrirModalExcluirEndereco(btn) {
  if (document.getElementById('idEnderecoParaExcluir')) document.getElementById('idEnderecoParaExcluir').value = btn.dataset.id;
  if (document.getElementById('txtEnderecoParaExcluir')) document.getElementById('txtEnderecoParaExcluir').textContent = btn.dataset.logradouro || '';

  var modalEl = document.getElementById('modalConfirmarExcluirEndereco');
  if (modalEl) new bootstrap.Modal(modalEl).show();
}

// Pré-preenche o modal de edição de endereço com os data-* do botão clicado.
function abrirModalEditarEndereco(btn) {
  if (document.getElementById('editEnderecoId')) document.getElementById('editEnderecoId').value = btn.dataset.id;
  if (document.getElementById('editEndNumero')) document.getElementById('editEndNumero').value = btn.dataset.numero || '';
  if (document.getElementById('editEndComplemento')) document.getElementById('editEndComplemento').value = btn.dataset.complemento || '';
  if (document.getElementById('editEndPrincipal')) document.getElementById('editEndPrincipal').checked = btn.dataset.principal === 'true';
  if (document.getElementById('editEndLogradouroDisplay')) document.getElementById('editEndLogradouroDisplay').value = btn.dataset.logradouro || '';

  // Re-aplica máscara sobre o valor existente (formata em vez de só aguardar nova digitação).
  if (document.getElementById('editEndTelefone')) {
    $('#editEndTelefone').val(btn.dataset.telefone || '').mask(maskTelefoneBehavior, maskTelefoneOpcoes);
  }

  var modalEl = document.getElementById('modalEditarEndereco');
  if (modalEl) new bootstrap.Modal(modalEl).show();
}

// Abre o modal de aviso explicando por que o endereço não pode ser excluído.
function mostrarAvisoNaoPodeDeletar(btn) {
  if (document.getElementById('txtAvisoEnderecoNaoPodeDeletar')) {
    document.getElementById('txtAvisoEnderecoNaoPodeDeletar').textContent = btn.dataset.motivo || 'Não é possível excluir este endereço.';
  }
  var modalEl = document.getElementById('modalAvisoEnderecoNaoPodeDeletar');
  if (modalEl) new bootstrap.Modal(modalEl).show();
}

/* ───────────────── Utilitários de UI ─────────────────
   S/N usa readonly em vez de disabled para que o valor chegue
   ao servidor no submit. */
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
}

// Faz o FeedbackPanel sumir após 5s e apaga os <li> internos
// pra evitar "fantasmas" em renderizações futuras do Wicket.
function autoHideFeedback() {
  setTimeout(function() {
    $('.feedbackPanel').fadeOut('slow', function() {
      $(this).empty();
      $(this).css('display', '');
    });
  }, 5000);
}

/* Aguarda até que uma dependência exista no window (jQuery, Bootstrap, Wicket).
   Necessário porque o clientes.js é injetado no <head> via PackageResourceReference,
   ANTES das tags <script> do final do <body> (jQuery Mask, Bootstrap CDN) e antes
   da bundle do Wicket-Ajax. Polling barato com fallback no DOMContentLoaded. */
function waitFor(check, callback, maxTentativas) {
  var t = 0, limite = maxTentativas || 200;
  (function tick() {
    if (check()) { callback(); return; }
    if (t++ > limite) return;          // desiste em silêncio após ~10s
    setTimeout(tick, 50);
  })();
}

// Init inicial: depende de jQuery + jQuery.mask + Bootstrap estarem prontos.
waitFor(
  function () { return window.jQuery && jQuery.fn && jQuery.fn.mask && window.bootstrap; },
  function () { initMasks(); autoHideFeedback(); }
);

/* ───────────────── Integração com AJAX do Wicket ─────────────────
   Após cada resposta AJAX, reaplica as máscaras nos novos elementos
   e reinicia o cronômetro do autoHideFeedback. */
waitFor(
  function () { return typeof Wicket !== 'undefined' && Wicket.Event && Wicket.Event.subscribe; },
  function () {
    Wicket.Event.subscribe('/ajax/call/complete', function (jqEvent, attributes, jqXHR, errorThrown, textStatus) {
      initMasks();
      autoHideFeedback();
    });
  }
);