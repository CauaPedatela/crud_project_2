/*
 * CardClientePanel — card principal da página de detalhes com avatar (iniciais),
 * nome, badges de tipo (PF/PJ) e status, e grid 2x3 com os dados do cliente
 * (nome/razão social, e-mail, ativo, CPF/CNPJ, RG/IE, data).
 *
 * Recebe um IModel<ClienteResponseDTO> compartilhado com a página-pai (mesmo
 * LoadableDetachableModel). Setando outputMarkupId=true, qualquer modal que
 * atualize o cliente pode chamar target.add(cardClientePanel) para re-renderizar.
 */
package com.crudproject.wicket.page.detalhes;

import com.crudproject.dto.cliente.ClienteResponseDTO;
import com.crudproject.model.TipoPessoa;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import java.time.format.DateTimeFormatter;

public class CardClientePanel extends Panel {

    private static final DateTimeFormatter FMT_DATA     = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FMT_CADASTRO = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");

    public CardClientePanel(String id, final IModel<ClienteResponseDTO> clienteModel) {
        super(id);
        setOutputMarkupId(true);

        add(new Label("avatarIniciais", new AbstractReadOnlyModel<String>() {
            @Override public String getObject() { return gerarIniciais(clienteModel.getObject().getNome()); }
        }));
        add(new Label("nomeCliente", new AbstractReadOnlyModel<String>() {
            @Override public String getObject() { return clienteModel.getObject().getNome(); }
        }));

        Label badgeTipo = new Label("badgeTipo", new AbstractReadOnlyModel<String>() {
            @Override public String getObject() {
                return clienteModel.getObject().getTipoPessoa() == TipoPessoa.FISICA ? "PF" : "PJ";
            }
        });
        badgeTipo.add(new AttributeAppender("class", new AbstractReadOnlyModel<String>() {
            @Override public String getObject() {
                return clienteModel.getObject().getTipoPessoa() == TipoPessoa.FISICA
                        ? " text-bg-info-subtle text-info-emphasis"
                        : " text-bg-warning-subtle text-warning-emphasis";
            }
        }, " "));
        add(badgeTipo);

        Label badgeAtivo = new Label("badgeAtivo", new AbstractReadOnlyModel<String>() {
            @Override public String getObject() {
                return Boolean.TRUE.equals(clienteModel.getObject().getAtivo()) ? "Ativo" : "Inativo";
            }
        });
        badgeAtivo.add(new AttributeAppender("class", new AbstractReadOnlyModel<String>() {
            @Override public String getObject() {
                return Boolean.TRUE.equals(clienteModel.getObject().getAtivo())
                        ? " text-bg-success-subtle text-success-emphasis"
                        : " text-bg-danger-subtle text-danger-emphasis";
            }
        }, " "));
        add(badgeAtivo);

        add(new Label("infoCliente", new AbstractReadOnlyModel<String>() {
            @Override public String getObject() {
                ClienteResponseDTO c = clienteModel.getObject();
                String dt = c.getDataCadastro() != null ? c.getDataCadastro().format(FMT_CADASTRO) : "—";
                return "Cliente #" + c.getId() + " · cadastrado em " + dt;
            }
        }));
        add(new Label("labelNome", new AbstractReadOnlyModel<String>() {
            @Override public String getObject() {
                return clienteModel.getObject().getTipoPessoa() == TipoPessoa.FISICA ? "Nome" : "Razão Social";
            }
        }));
        add(new Label("valorNome", new AbstractReadOnlyModel<String>() {
            @Override public String getObject() { return nvl(clienteModel.getObject().getNome()); }
        }));
        add(new Label("valorEmail", new AbstractReadOnlyModel<String>() {
            @Override public String getObject() { return nvl(clienteModel.getObject().getEmail()); }
        }));
        add(new Label("valorAtivo", new AbstractReadOnlyModel<String>() {
            @Override public String getObject() {
                return Boolean.TRUE.equals(clienteModel.getObject().getAtivo()) ? "Sim" : "Não";
            }
        }));
        add(new Label("labelDocumento", new AbstractReadOnlyModel<String>() {
            @Override public String getObject() {
                return clienteModel.getObject().getTipoPessoa() == TipoPessoa.FISICA ? "CPF" : "CNPJ";
            }
        }));
        add(new Label("valorDocumento", new AbstractReadOnlyModel<String>() {
            @Override public String getObject() { return nvl(clienteModel.getObject().getCpfCnpj()); }
        }));
        add(new Label("labelIdentificacao", new AbstractReadOnlyModel<String>() {
            @Override public String getObject() {
                return clienteModel.getObject().getTipoPessoa() == TipoPessoa.FISICA ? "RG" : "Inscrição Estadual";
            }
        }));
        add(new Label("valorIdentificacao", new AbstractReadOnlyModel<String>() {
            @Override public String getObject() { return nvl(clienteModel.getObject().getRgInscricaoEstadual()); }
        }));
        add(new Label("labelData", new AbstractReadOnlyModel<String>() {
            @Override public String getObject() {
                return clienteModel.getObject().getTipoPessoa() == TipoPessoa.FISICA
                        ? "Data de Nascimento" : "Data de Criação";
            }
        }));
        add(new Label("valorData", new AbstractReadOnlyModel<String>() {
            @Override public String getObject() {
                java.time.LocalDate d = clienteModel.getObject().getDataNascimento();
                return d != null ? d.format(FMT_DATA) : "—";
            }
        }));
    }

    private static String gerarIniciais(String nome) {
        if (nome == null || nome.isBlank()) return "?";
        String[] partes = nome.trim().split("\\s+");
        if (partes.length == 1) return partes[0].substring(0, Math.min(2, partes[0].length())).toUpperCase();
        return ("" + partes[0].charAt(0) + partes[partes.length - 1].charAt(0)).toUpperCase();
    }

    private static String nvl(String v) {
        return (v != null && !v.isBlank()) ? v : "—";
    }
}
