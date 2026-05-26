/*
 * Resources — classe marcadora vazia no mesmo pacote dos arquivos
 * clientes.css e clientes.js. Serve como "scope" para PackageResourceReference,
 * permitindo referenciá-los com nome relativo (sem path absoluto) e garantindo
 * que o SecurePackageResourceGuard do Wicket aceite acessar os arquivos.
 *
 * Equivale ao "barrel index" do Angular: existe só pra dar identidade ao pacote.
 */
package com.crudproject.wicket.resources;

public final class Resources {
    private Resources() { /* não instanciável */ }
}
