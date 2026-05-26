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

/* ───────────────── Integração ViaCEP ───────────────── */
function buscarCepNoBloco(input) {
  var cep = input.value.replace(/\D/g, '');
  if (cep.length !== 8) return;

  // A trava cirúrgica: garante que preenchemos apenas o endereço atual
  var bloco = input.closest('.bloco-endereco');
  if (!bloco) return;

  fetch('https://viacep.com.br/ws/' + cep + '/json/')
    .then(function(r) { return r.json(); })
    .then(function(data) {
      if (data.erro) {
        // Aviso amigável no console para não acharmos que o código quebrou!
        console.warn("ViaCEP: O CEP " + cep + " não foi encontrado na base dos Correios.");
        return;
      }

      var set = function(cls, val) {
        var el = bloco.querySelector('.' + cls);
        if (el && val) el.value = val;
      };

      set('viacep-logradouro', data.logradouro);
      set('viacep-bairro',     data.bairro);
      set('viacep-cidade',     data.localidade);

      // O Wicket pode usar o texto ou o value numérico nas options de Estado
      if (data.uf) {
        var sel = bloco.querySelector('.viacep-estado');
        if (sel) {
          for (var i = 0; i < sel.options.length; i++) {
            if (sel.options[i].text === data.uf || sel.options[i].value === data.uf) {
                sel.selectedIndex = i;
                break;
            }
          }
        }
      }
    })
    .catch(function(e) {
        console.error("Erro ao consultar o ViaCEP: ", e);
    });
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
  if (document.getElementById('editEnderecoId')) document.getElementById('editEnderecoId').value = btn.dataset.id;
  if (document.getElementById('editEndNumero')) document.getElementById('editEndNumero').value = btn.dataset.numero || '';
  if (document.getElementById('editEndComplemento')) document.getElementById('editEndComplemento').value = btn.dataset.complemento || '';
  if (document.getElementById('editEndPrincipal')) document.getElementById('editEndPrincipal').checked = btn.dataset.principal === 'true';
  if (document.getElementById('editEndLogradouroDisplay')) document.getElementById('editEndLogradouroDisplay').value = btn.dataset.logradouro || '';

  if (document.getElementById('editEndTelefone')) {
    $('#editEndTelefone').val(btn.dataset.telefone || '').mask(maskTelefoneBehavior, maskTelefoneOpcoes);
  }

  var modalEl = document.getElementById('modalEditarEndereco');
  if (modalEl) new bootstrap.Modal(modalEl).show();
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