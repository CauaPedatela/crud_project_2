package com.crudproject.model;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Representa um Cliente no sistema.
 *
 * Pode ser Pessoa Física (CPF) ou Pessoa Jurídica (CNPJ).
 * Campos como CPF/RG são exclusivos de PF, enquanto
 * CNPJ/Razão Social são exclusivos de PJ.
 *
 * Um Cliente pode ter vários Endereços (relação 1:N).
 */
@Entity
@Table(name = "tb_cliente") // define o nome da tabela no banco
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pessoa", nullable = false)
    private TipoPessoa tipoPessoa;

    // -------------------------
    // Campos exclusivos de PF
    // -------------------------

    @Column(name = "cpf", length = 14)
    private String cpf;

    @Column(name = "nome", length = 150)
    private String nome;

    @Column(name = "rg", length = 20)
    private String rg;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    // -------------------------
    // Campos exclusivos de PJ
    // -------------------------

    @Column(name = "cnpj", length = 18)
    private String cnpj;

    @Column(name = "razao_social", length = 150)
    private String razaoSocial;

    @Column(name = "inscricao_estadual", length = 20)
    private String inscricaoEstadual;

    @Column(name = "data_criacao")
    private LocalDate dataCriacao;

    // -------------------------
    // Campos comuns (PF e PJ)
    // -------------------------

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo;

    /**
     * Lista de endereços do cliente.
     *
     * @OneToMany → um Cliente tem muitos Endereços
     * mappedBy = "cliente" → diz que o lado "dono" do relacionamento
     *   é o campo "cliente" dentro da classe Endereço
     * cascade → operações no Cliente se propagam para os Endereços
     *   (ex: ao salvar o Cliente, salva os Endereços automaticamente)
     */
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL)
    private List<Endereco> enderecos;

    // -------------------------
    // Getters e Setters
    // -------------------------

    public Long getId() { return id; }
    public void setId(Long id) {
        this.id = id;
    }

    public TipoPessoa getTipoPessoa() {
        return tipoPessoa;
    }
    public void setTipoPessoa(TipoPessoa tipoPessoa) {
        this.tipoPessoa = tipoPessoa;
    }

    public String getCpf() {
        return cpf;
    }
    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getRg() {
        return rg;
    }
    public void setRg(String rg) {
        this.rg = rg;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }
    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public String getCnpj() {
        return cnpj;
    }
    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getRazaoSocial() {
        return razaoSocial;
    }
    public void setRazaoSocial(String razaoSocial) {
        this.razaoSocial = razaoSocial;
    }

    public String getInscricaoEstadual() {
        return inscricaoEstadual;
    }
    public void setInscricaoEstadual(String inscricaoEstadual) {
        this.inscricaoEstadual = inscricaoEstadual;
    }

    public LocalDate getDataCriacao() {
        return dataCriacao;
    }
    public void setDataCriacao(LocalDate dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getAtivo() {
        return ativo;
    }
    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public List<Endereco> getEnderecos() {
        return enderecos;
    }
    public void setEnderecos(List<Endereco> enderecos) {
        this.enderecos = enderecos;
    }

    // -------------------------
    // Métodos auxiliares
    // -------------------------

    public boolean isPessoaFisica() {
        return this.tipoPessoa == TipoPessoa.FISICA;
    }

    public boolean isPessoaJuridica() {
        return this.tipoPessoa == TipoPessoa.JURIDICA;
    }
}
