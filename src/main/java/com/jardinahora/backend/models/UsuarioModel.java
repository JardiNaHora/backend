package com.jardinahora.backend.models;

import jakarta.persistence.*;
import org.springframework.hateoas.RepresentationModel;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "TB_USUARIO")
public class UsuarioModel extends RepresentationModel<UsuarioModel> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private String nome;
    private String email;
    private String senha;

    @Column(name = "funcao")
    @Enumerated(EnumType.STRING)
    private FuncaoUsuarioModel funcao;

    @Column(name = "fonte_registro")
    @Enumerated(EnumType.STRING)
    private FonteRegistroModel fonteRegistro;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public FuncaoUsuarioModel getFuncao() {
        return funcao;
    }

    public void setFuncao(FuncaoUsuarioModel funcao) {
        this.funcao = funcao;
    }

    public FonteRegistroModel getFonteRegistro() {
        return fonteRegistro;
    }

    public void setFonteRegistro(FonteRegistroModel fonteRegistro) {
        this.fonteRegistro = fonteRegistro;
    }

}
