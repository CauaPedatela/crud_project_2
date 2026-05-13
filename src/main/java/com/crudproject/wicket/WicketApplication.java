package com.crudproject.wicket;

import com.crudproject.wicket.page.ListagemClientesPage;
import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;

public class WicketApplication extends WebApplication {

    @Override
    public Class<? extends Page> getHomePage() {
        return ListagemClientesPage.class;
    }

    @Override
    public void init() {
        super.init();
    }
}
