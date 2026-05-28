package com.crudproject.dto;

import java.io.Serializable;

// DTO com os contadores agregados de clientes — alimenta o header da listagem
// SEM precisar carregar nenhuma entidade na memória do servidor.
// Implementa Serializable porque o Wicket pode armazená-lo na sessão HTTP.
public class ContadoresDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private long total;   // total de clientes no banco (vem de COUNT(*))
    private long ativos;  // total de clientes com ativo = true (vem de COUNT WHERE ativo=true)

    public ContadoresDTO() {}

    public ContadoresDTO(long total, long ativos) {
        this.total = total;
        this.ativos = ativos;
    }

    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }

    public long getAtivos() { return ativos; }
    public void setAtivos(long ativos) { this.ativos = ativos; }
}
