/*
 * EnderecosListaPanel — lista de endereços do cliente com botões editar/excluir
 * por item e botão "Adicionar Endereço" no rodapé.
 *
 * Os botões editar/excluir não fazem submit Wicket — apenas montam data-*
 * attributes que o JS (abrirModalEditarEndereco / abrirModalExcluirEndereco)
 * usa pra preencher e abrir os modais correspondentes da página.
 *
 * O botão excluir fica visualmente desabilitado quando o endereço é principal
 * ou é o único — clicando neste estado abre o modal de aviso (puro Bootstrap).
 *
 * Setando outputMarkupId=true, qualquer modal de endereço (criar, editar,
 * excluir) pode chamar target.add(enderecosListaPanel) após salvar.
 */
package com.crudproject.wicket.page.detalhes;

import com.crudproject.dto.cliente.ClienteResponseDTO;
import com.crudproject.dto.endereco.EnderecoResponseDTO;
import com.crudproject.model.TipoEndereco;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.util.List;

public class EnderecosListaPanel extends Panel {

    public EnderecosListaPanel(String id, final IModel<ClienteResponseDTO> clienteModel) {
        super(id);
        setOutputMarkupId(true);

        ListView<EnderecoResponseDTO> listaEnderecos = new ListView<EnderecoResponseDTO>(
                "listaEnderecos",
                new AbstractReadOnlyModel<List<EnderecoResponseDTO>>() {
                    @Override
                    public List<EnderecoResponseDTO> getObject() {
                        return clienteModel.getObject().getEnderecos();
                    }
                }) {
            @Override
            protected void populateItem(ListItem<EnderecoResponseDTO> item) {
                EnderecoResponseDTO end = item.getModelObject();
                boolean isPrincipal = Boolean.TRUE.equals(end.getPrincipal());
                int     totalEnds   = clienteModel.getObject().getEnderecos().size();
                boolean podeDeletar = !isPrincipal && totalEnds > 1;

                Label tipoLabel = new Label("endTipo",
                        end.getTipo() != null ? end.getTipo().name() : "—");
                tipoLabel.add(new AttributeAppender("class", Model.of(
                        end.getTipo() == TipoEndereco.RESIDENCIAL
                                ? " text-bg-success-subtle text-success-emphasis"
                                : " text-bg-warning-subtle text-warning-emphasis"), " "));
                item.add(tipoLabel);

                Label principalLabel = new Label("endPrincipal", isPrincipal ? "Principal" : "Secundário");
                principalLabel.add(new AttributeAppender("class", Model.of(
                        isPrincipal ? " bg-primary text-white"
                                : " text-bg-secondary-subtle text-secondary-emphasis"), " "));
                item.add(principalLabel);

                item.add(new Label("endLogradouro",     nvl(end.getLogradouro())));
                item.add(new Label("endNumeroEndereco", nvl(end.getNumero())));
                item.add(new Label("endComplemento",    nvl(end.getComplemento())));
                item.add(new Label("endBairro",         nvl(end.getBairro())));
                item.add(new Label("endCidade",         nvl(end.getCidade())));
                item.add(new Label("endEstado",         nvl(end.getEstado())));
                item.add(new Label("endCep",            nvl(end.getCep())));
                item.add(new Label("endPais",           nvl(end.getPais())));
                item.add(new Label("endTelefone",       nvl(end.getTelefone())));

                // Botão "Editar" — agora carrega TODOS os campos do endereço via data-*
                // porque o modal de editar passou a permitir editar tudo (não só número/
                // complemento/telefone/principal como antes).
                WebMarkupContainer btnEditar = new WebMarkupContainer("btnEditarEndereco");
                btnEditar.add(AttributeModifier.replace("onclick",          "abrirModalEditarEndereco(this)"));
                btnEditar.add(AttributeModifier.replace("data-id",          String.valueOf(end.getId())));
                btnEditar.add(AttributeModifier.replace("data-logradouro",  safe(end.getLogradouro())));
                btnEditar.add(AttributeModifier.replace("data-numero",      safe(end.getNumero())));
                btnEditar.add(AttributeModifier.replace("data-complemento", safe(end.getComplemento())));
                btnEditar.add(AttributeModifier.replace("data-bairro",      safe(end.getBairro())));
                btnEditar.add(AttributeModifier.replace("data-cidade",      safe(end.getCidade())));
                btnEditar.add(AttributeModifier.replace("data-estado",      safe(end.getEstado())));
                btnEditar.add(AttributeModifier.replace("data-cep",         safe(end.getCep())));
                btnEditar.add(AttributeModifier.replace("data-pais",        safe(end.getPais())));
                btnEditar.add(AttributeModifier.replace("data-telefone",    safe(end.getTelefone())));
                btnEditar.add(AttributeModifier.replace("data-principal",   String.valueOf(isPrincipal)));
                item.add(btnEditar);

                WebMarkupContainer btnExcluir = new WebMarkupContainer("btnExcluirEndereco");
                if (podeDeletar) {
                    btnExcluir.add(AttributeModifier.replace("onclick",
                            "abrirModalExcluirEndereco(this)"));
                    btnExcluir.add(AttributeModifier.replace("data-id",
                            String.valueOf(end.getId())));
                    btnExcluir.add(AttributeModifier.replace("data-logradouro",
                            safe(end.getLogradouro())));
                } else {
                    String motivo = isPrincipal
                            ? "Este é o endereço principal. Para excluí-lo, primeiro defina outro endereço como principal."
                            : "O cliente deve ter pelo menos um endereço cadastrado.";
                    btnExcluir.add(AttributeModifier.replace("onclick", "mostrarAvisoNaoPodeDeletar(this)"));
                    btnExcluir.add(AttributeModifier.replace("data-motivo", motivo));
                    btnExcluir.add(new AttributeAppender("class", Model.of(" opacity-50"), ""));
                }
                item.add(btnExcluir);
            }
        };

        add(listaEnderecos);
    }

    private static String nvl(String v) {
        return (v != null && !v.isBlank()) ? v : "—";
    }

    private static String safe(String v) {
        return v != null ? v : "";
    }
}
