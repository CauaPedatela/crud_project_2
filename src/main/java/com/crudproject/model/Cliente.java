package com.crudproject.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

//  Representa um Cliente no sistema. Os campos equivalentes foram unificados.
//
//    FISICA   →  nome = nome civil
//                cpfCnpj = CPF (11 dígitos)
//                rgInscricaoEstadual = RG
//                dataNascimento = data de nascimento
//
//    JURIDICA →  nome = razão social
//                cpfCnpj = CNPJ (14 dígitos)
//                rgInscricaoEstadual = Inscrição Estadual
//                dataNascimento = data de fundação
//
//  Um Cliente pode ter vários Endereços (relação 1:N).

@Entity
@Table(name = "tb_cliente")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pessoa", nullable = false)
    private TipoPessoa tipoPessoa;

    @Column(name = "nome", nullable = false, length = 150)
    private String nome;

    @Column(name = "cpf_cnpj", nullable = false, length = 14, unique = true)
    private String cpfCnpj;

    @Column(name = "rg_inscricao_estadual", length = 20)
    private String rgInscricaoEstadual;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo;

    @Column(name = "data_cadastro", nullable = false)
    private LocalDateTime dataCadastro;

    //LISTA DE ENDEREÇOS POR CLIENTE.
    //cascade = ALL          → operações no cliente cascateiam para os endereços (save salva todos juntos; delete apaga todos)
    //orphanRemoval = true   → endereço removido da lista é deletado do banco (essencial para o sync no PUT)
    //@JsonManagedReference  → serializa esse lado da relação para JSON

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Endereco> enderecos;

    // ============================================================
    // Getters e Setters
    // ============================================================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public TipoPessoa getTipoPessoa() { return tipoPessoa; }
    public void setTipoPessoa(TipoPessoa tipoPessoa) { this.tipoPessoa = tipoPessoa; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCpfCnpj() { return cpfCnpj; }
    public void setCpfCnpj(String cpfCnpj) { this.cpfCnpj = cpfCnpj; }

    public String getRgInscricaoEstadual() { return rgInscricaoEstadual; }
    public void setRgInscricaoEstadual(String rgInscricaoEstadual) { this.rgInscricaoEstadual = rgInscricaoEstadual; }

    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }

    public LocalDateTime getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(LocalDateTime dataCadastro) { this.dataCadastro = dataCadastro; }

    public List<Endereco> getEnderecos() { return enderecos; }
    public void setEnderecos(List<Endereco> enderecos) { this.enderecos = enderecos; }

    // ============================================================
    // Helpers
    // ============================================================

    public boolean isPessoaFisica() { return this.tipoPessoa == TipoPessoa.FISICA; }
    public boolean isPessoaJuridica() { return this.tipoPessoa == TipoPessoa.JURIDICA; }
}
