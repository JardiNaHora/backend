package com.jardinahora.backend.models;

import jakarta.persistence.*;
import org.springframework.hateoas.RepresentationModel;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "TB_SPECIAL_OCCURRENCE")
public class SpecialOccurrence extends RepresentationModel<SpecialOccurrence> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private Date DATE;
    private String reason;

    public UUID getId() {
        return id;
    }

    public Date getDATE() {
        return DATE;
    }

    public void setDATE(Date date) {
        this.DATE = date;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

}
