/*
 * ClienteDataProvider — ponte entre o DataView do Wicket e o ClienteService paginado.
 *
 * O Wicket chama dois métodos durante o render:
 *   - size()                       → quantos itens existem no total? (define o nº de páginas)
 *   - iterator(first, count)       → me dá os itens [first .. first+count)
 *
 * Aqui convertemos esses dois métodos para chamadas paginadas ao service —
 * apenas a página atual é trazida do banco, nunca a lista completa.
 */
package com.crudproject.wicket.page;

import com.crudproject.dto.PageResponseDTO;
import com.crudproject.dto.cliente.ClienteResponseDTO;
import com.crudproject.service.ClienteService;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Iterator;

public class ClienteDataProvider implements IDataProvider<ClienteResponseDTO> {

    // Estado dos filtros — referência compartilhada com BuscaPanel/FiltrosPanel.
    // Como FiltroState é Serializable e nós só guardamos um ponteiro, não há problema
    // de serialização entre requests do Wicket.
    private final FiltroState filtros;

    // Service injetado via construtor (vem do @SpringBean do panel pai).
    // O Wicket-Spring envolve o bean em um proxy serializável, então pode ficar como campo.
    private final ClienteService clienteService;

    // Cache do total dentro do MESMO request — evita duas queries de count quando
    // o Wicket chama size() múltiplas vezes (uma pelo navegador, outra pelo Label).
    // É limpo no detach() ao final do request.
    private Long totalCache;

    public ClienteDataProvider(FiltroState filtros, ClienteService clienteService) {
        this.filtros = filtros;
        this.clienteService = clienteService;
    }

    @Override
    public Iterator<? extends ClienteResponseDTO> iterator(long first, long count) {
        // O Wicket trabalha com "começo + quantidade"; o Spring Data com "page + size".
        // Como o tamanho da página é fixo no DataView (10), first é sempre múltiplo de count.
        int page = (int) (first / count);
        int size = (int) count;

        PageResponseDTO<ClienteResponseDTO> resposta = clienteService.buscarComFiltrosPaginado(
                filtros.getTermoBusca(),
                filtros.getFiltroAtivo(),
                filtros.getFiltroTipo(),
                filtros.getDataCriacaoInicio(),
                filtros.getDataCriacaoFim(),
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dataCadastro"))
        );

        // Cache o total que veio "de graça" junto com a página — evita uma query extra no size().
        this.totalCache = resposta.getTotalElements();

        return resposta.getContent().iterator();
    }

    @Override
    public long size() {
        if (totalCache == null) {
            // Se iterator() ainda não foi chamado (ex: cálculo de quantas páginas existem),
            // disparamos um count() leve no banco — sem buscar nenhum cliente.
            totalCache = clienteService.contarComFiltros(
                    filtros.getTermoBusca(),
                    filtros.getFiltroAtivo(),
                    filtros.getFiltroTipo(),
                    filtros.getDataCriacaoInicio(),
                    filtros.getDataCriacaoFim());
        }
        return totalCache;
    }

    @Override
    public IModel<ClienteResponseDTO> model(ClienteResponseDTO object) {
        // ClienteResponseDTO já é Serializable, então um Model simples basta.
        return Model.of(object);
    }

    @Override
    public void detach() {
        // Limpa o cache ao final do request. Crítico para refletir mudanças
        // após criar/editar/excluir cliente — o próximo render fará count() de novo.
        totalCache = null;
    }
}
