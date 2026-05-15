package com.crudproject.wicket;

import com.crudproject.wicket.page.ListagemClientesPage;
import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;

public class WicketApplication extends WebApplication {

    @Override
    public Class<? extends Page> getHomePage() {
        return ListagemClientesPage.class;
    }

    @Override
    public void init() {
        super.init();
        // Habilita @SpringBean em todas as páginas e componentes Wicket.
        // Sem essa linha, o @SpringBean não funciona.
        getComponentInstantiationListeners().add(new SpringComponentInjector(this));
    }
}
