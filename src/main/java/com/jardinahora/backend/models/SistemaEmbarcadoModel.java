package com.jardinahora.backend.models;

import jakarta.persistence.*;
import org.springframework.hateoas.RepresentationModel;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "TB_SISTEMAEMBARCADO")
 class SistemaEmbarcadoModel extends RepresentationModel<SistemaEmbarcadoModel> implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private String nome;
    private Double sensorGiroscopio;
    private Double sensorAcelerometro;
    private String posicaoGPS;
    private Date horaColeta;

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

    public Double getSensorGiroscopio() {
        return sensorGiroscopio;
    }

    public void setSensorGiroscopio(Double sensorGiroscopio) {
        this.sensorGiroscopio = sensorGiroscopio;
    }

    public Double getSensorAcelerometro() {
        return sensorAcelerometro;
    }

    public void setSensorAcelerometro(Double sensorAcelerometro) {
        this.sensorAcelerometro = sensorAcelerometro;
    }

    public String getPosicaoGPS() {
        return posicaoGPS;
    }

    public void setPosicaoGPS(String posicaoGPS) {
        this.posicaoGPS = posicaoGPS;
    }

    public Date getHoraColeta() {
        return horaColeta;
    }

    public void setHoraColeta(Date horaColeta) {
        this.horaColeta = horaColeta;
    }

}
