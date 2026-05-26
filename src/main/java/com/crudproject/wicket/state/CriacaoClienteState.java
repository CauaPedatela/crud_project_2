/*
 * CriacaoClienteState — agrupa todos os campos editáveis do modal "Cadastrar
 * Novo Cliente". Substitui as ~7 propriedades soltas da página + a lista
 * dinâmica de endereços por um único POJO Serializable, que é exatamente
 * o formato JSON que o futuro frontend Angular enviará ao POST /api/clientes.
 */
package com.crudproject.wicket.state;

import com.crudproject.dto.cliente.ClienteDTO;
import com.crudproject.dto.endereco.EnderecoDTO;
import com.crudproject.model.TipoEndereco;
import com.crudproject.model.TipoPessoa;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CriacaoClienteState implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FMT_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private TipoPessoa        tipoPessoa = TipoPessoa.FISICA;
    private String            nome;
    private String            cpfCnpj;
    private String            rgIe;
    private String            data;            // String "dd/MM/yyyy" — parseada no submit
    private String            email;
    private Boolean           ativo = true;
    private List<EnderecoDTO> enderecos = novaListaInicial();

    public static CriacaoClienteState inicial() {
        return new CriacaoClienteState();
    }

    public void resetar() {
        this.tipoPessoa = TipoPessoa.FISICA;
        this.nome       = null;
        this.cpfCnpj    = null;
        this.rgIe       = null;
        this.data       = null;
        this.email      = null;
        this.ativo      = true;
        this.enderecos  = novaListaInicial();
    }

    // Converte o estado atual em ClienteDTO pronto para o ClienteService.salvar().
    // Faz a validação do formato da data (espera "dd/MM/yyyy") e garante país default.
    public ClienteDTO toDto() {
        ClienteDTO dto = new ClienteDTO();
        dto.setTipoPessoa(tipoPessoa);
        dto.setNome(nome);
        dto.setCpfCnpj(cpfCnpj);
        dto.setRgInscricaoEstadual(rgIe);

        if (data != null && !data.trim().isEmpty()) {
            try {
                dto.setDataNascimento(LocalDate.parse(data.trim(), FMT_BR));
            } catch (Exception e) {
                throw new RuntimeException("Data inválida. Use o formato dd/mm/aaaa.");
            }
        }

        dto.setAtivo(ativo != null ? ativo : false);
        dto.setEmail(email);

        for (EnderecoDTO e : enderecos) {
            if (e.getPais() == null || e.getPais().isBlank()) e.setPais("Brasil");
        }
        dto.setEnderecos(new ArrayList<>(enderecos));
        return dto;
    }

    private static List<EnderecoDTO> novaListaInicial() {
        List<EnderecoDTO> lista = new ArrayList<>();
        EnderecoDTO primeiro = new EnderecoDTO();
        primeiro.setTipo(TipoEndereco.RESIDENCIAL);
        primeiro.setPais("Brasil");
        primeiro.setPrincipal(true);
        lista.add(primeiro);
        return lista;
    }

    public TipoPessoa        getTipoPessoa()                    { return tipoPessoa; }
    public void              setTipoPessoa(TipoPessoa v)        { this.tipoPessoa = v; }

    public String            getNome()                          { return nome; }
    public void              setNome(String v)                  { this.nome = v; }

    public String            getCpfCnpj()                       { return cpfCnpj; }
    public void              setCpfCnpj(String v)               { this.cpfCnpj = v; }

    public String            getRgIe()                          { return rgIe; }
    public void              setRgIe(String v)                  { this.rgIe = v; }

    public String            getData()                          { return data; }
    public void              setData(String v)                  { this.data = v; }

    public String            getEmail()                         { return email; }
    public void              setEmail(String v)                 { this.email = v; }

    public Boolean           getAtivo()                         { return ativo; }
    public void              setAtivo(Boolean v)                { this.ativo = v; }

    public List<EnderecoDTO> getEnderecos()                     { return enderecos; }
    public void              setEnderecos(List<EnderecoDTO> v)  { this.enderecos = v; }
}
