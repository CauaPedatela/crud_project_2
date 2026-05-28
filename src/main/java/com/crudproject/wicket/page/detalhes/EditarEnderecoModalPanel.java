/*
 * EditarEnderecoModalPanel — modal de edição de endereço.
 *
 * A partir desta versão, TODOS os campos do endereço são editáveis:
 * logradouro, número, complemento, bairro, cidade, estado, CEP, país,
 * telefone e status "principal". O tipo (RESIDENCIAL/COMERCIAL) continua
 * fixo — para trocar, basta excluir e criar de novo.
 *
 * O JS abrirModalEditarEndereco(btn) pré-preenche todos os campos via
 * data-* attributes. Ao alterar o CEP, o ViaCEP é consultado automaticamente
 * (mesma UX do modal de criar) para reabastecer logradouro/bairro/cidade/UF.
 *
 * No submit, busca o cliente, substitui o endereço alvo com os novos valores,
 * aplica a regra de "principal único" e chama clienteService.atualizar().
 */
package com.crudproject.wicket.page.detalhes;

import com.crudproject.dto.cliente.ClienteResponseDTO;
import com.crudproject.dto.endereco.EnderecoDTO;
import com.crudproject.service.ClienteService;
import com.crudproject.wicket.state.ClienteDtoBuilder;
import com.crudproject.wicket.state.EdicaoEnderecoState;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.util.List;

public class EditarEnderecoModalPanel extends Panel {

    @SpringBean
    private ClienteService clienteService;

    private final EdicaoEnderecoState state = new EdicaoEnderecoState();

    public EditarEnderecoModalPanel(String id,
                                    final IModel<ClienteResponseDTO> clienteModel,
                                    final FeedbackPanel feedbackPagina,
                                    final Component... componentesParaAtualizar) {
        super(id);

        Form<Void> form = new Form<>("formEditarEndereco");

        final FeedbackPanel feedbackModal = new FeedbackPanel("feedbackEditarEndereco",
                new ComponentFeedbackMessageFilter(form));
        feedbackModal.setOutputMarkupId(true);
        form.add(feedbackModal);

        HiddenField<Long> hiddenId = new HiddenField<>("hiddenEditarEnderecoId",
                new PropertyModel<Long>(state, "idEndereco"), Long.class);
        hiddenId.setMarkupId("editEnderecoId");
        hiddenId.setOutputMarkupId(true);
        form.add(hiddenId);

        // ────────────────────────────────────────────────────────
        // Campos que agora são editáveis (antes ficavam read-only)
        // ────────────────────────────────────────────────────────
        TextField<String> tfLogradouro = bindTexto(form, "editEndLogradouro", "logradouro");
        TextField<String> tfBairro     = bindTexto(form, "editEndBairro",     "bairro");
        TextField<String> tfCep        = bindTexto(form, "editEndCep",        "cep");
        TextField<String> tfPais       = bindTexto(form, "editEndPais",       "pais");

        // Estado e Cidade são DROPDOWNS DINÂMICOS via IBGE.
        // O usuário escolhe num <select> puro HTML — controlado por JS — e o
        // JS mantém estes HiddenFields em sincronia. O Wicket então lê o valor
        // submetido normalmente, como se fosse um TextField.
        // (HiddenField é a melhor solução para listas dinâmicas em Wicket,
        // já que DropDownChoice valida contra uma lista estática.)
        HiddenField<String> hfEstado = new HiddenField<>("editEndEstado",
                new PropertyModel<>(state, "estado"));
        hfEstado.setMarkupId("editEndEstado");
        hfEstado.setOutputMarkupId(true);
        form.add(hfEstado);

        HiddenField<String> hfCidade = new HiddenField<>("editEndCidade",
                new PropertyModel<>(state, "cidade"));
        hfCidade.setMarkupId("editEndCidade");
        hfCidade.setOutputMarkupId(true);
        form.add(hfCidade);

        // ────────────────────────────────────────────────────────
        // Campos que já eram editáveis
        // ────────────────────────────────────────────────────────
        TextField<String> tfNumero      = bindTexto(form, "editEndNumero",      "numero");
        TextField<String> tfComplemento = bindTexto(form, "editEndComplemento", "complemento");
        TextField<String> tfTelefone    = bindTexto(form, "editEndTelefone",    "telefone");

        CheckBox cbPrincipal = new CheckBox("editEndPrincipal",
                new PropertyModel<>(state, "principal"));
        cbPrincipal.setMarkupId("editEndPrincipal");
        cbPrincipal.setOutputMarkupId(true);
        form.add(cbPrincipal);

        form.add(new AjaxButton("btnSalvarEditarEndereco", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> formClicked) {
                if (state.getIdEndereco() == null) return;
                try {
                    ClienteResponseDTO current = clienteModel.getObject();
                    List<EnderecoDTO> novosEnderecos = ClienteDtoBuilder.toEnderecosDTOs(current.getEnderecos());

                    EnderecoDTO alvo = novosEnderecos.stream()
                            .filter(e -> state.getIdEndereco().equals(e.getId()))
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("Endereço não encontrado."));

                    // Aplica TODOS os campos editáveis (incluindo os que antes eram fixos).
                    // O validator do service vai recusar valores inválidos (bairro vazio, CEP
                    // mal formado, etc) com mensagem clara.
                    alvo.setLogradouro(state.getLogradouro());
                    alvo.setBairro(state.getBairro());
                    alvo.setCidade(state.getCidade());
                    alvo.setEstado(state.getEstado());
                    alvo.setCep(state.getCep());
                    alvo.setPais(state.getPais());
                    alvo.setNumero(state.getNumero());
                    alvo.setComplemento(state.getComplemento());
                    alvo.setTelefone(state.getTelefone());

                    if (Boolean.TRUE.equals(state.getPrincipal())) {
                        novosEnderecos.forEach(e -> e.setPrincipal(false));
                        alvo.setPrincipal(true);
                    } else {
                        boolean temOutroPrincipal = novosEnderecos.stream()
                                .anyMatch(e -> !state.getIdEndereco().equals(e.getId())
                                        && Boolean.TRUE.equals(e.getPrincipal()));
                        if (!temOutroPrincipal && Boolean.TRUE.equals(alvo.getPrincipal())) {
                            throw new RuntimeException(
                                    "Não é possível desmarcar o único endereço principal. " +
                                            "Defina outro endereço como principal antes.");
                        }
                        alvo.setPrincipal(false);
                    }

                    clienteService.atualizar(current.getId(),
                            ClienteDtoBuilder.comEnderecos(current, novosEnderecos));
                    clienteModel.detach();

                    EditarEnderecoModalPanel.this.getPage().info("Endereço atualizado com sucesso.");
                    target.add(feedbackPagina);
                    for (Component c : componentesParaAtualizar) {
                        target.add(c);
                    }
                    target.appendJavaScript(
                            "var m = bootstrap.Modal.getInstance(document.getElementById('modalEditarEndereco'));" +
                                    "if (m) m.hide();");
                } catch (Exception ex) {
                    form.error("Erro ao editar endereço: " + ex.getMessage());
                    target.add(feedbackModal);
                }
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> formClicked) {
                target.add(feedbackModal);
            }
        });

        add(form);
    }

    /**
     * Helper privado para reduzir repetição: cria um TextField com markupId
     * (necessário para o JS pré-preencher) e o adiciona ao form. Como todos
     * os campos editáveis seguem o mesmo padrão, isso evita 5 blocos idênticos.
     */
    private TextField<String> bindTexto(Form<Void> form, String markupId, String propriedade) {
        TextField<String> tf = new TextField<>(markupId, new PropertyModel<>(state, propriedade));
        tf.setMarkupId(markupId);
        tf.setOutputMarkupId(true);
        form.add(tf);
        return tf;
    }
}
