package com.jardinahora.backend.models;

import jakarta.persistence.*;
import org.springframework.hateoas.RepresentationModel;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "TB_VIAGEM")
public class ViagemModel extends RepresentationModel<ViagemModel> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private String percurso;
    private Date horarioPartida;
    private Date previsaoChegada;
    private Date dataViagem;
    private Integer numViagem;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getPercurso() {
        return percurso;
    }

    public void setPercurso(String percurso) {
        this.percurso = percurso;
    }

    public Date getHorarioPartida() {
        return horarioPartida;
    }

    public void setHorarioPartida(Date horarioPartida) {
        this.horarioPartida = horarioPartida;
    }

    public Date getPrevisaoChegada() {
        return previsaoChegada;
    }

    public void setPrevisaoChegada(Date previsaoChegada) {
        this.previsaoChegada = previsaoChegada;
    }

    public Date getDataViagem() {
        return dataViagem;
    }

    public void setDataViagem(Date dataViagem) {
        this.dataViagem = dataViagem;
    }

    public Integer getNumViagem() {
        return numViagem;
    }

    public void setNumViagem(Integer numViagem) {
        this.numViagem = numViagem;
    }

}
