package com.jardinahora.backend.models;

import jakarta.persistence.*;
import org.springframework.hateoas.RepresentationModel;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "TB_EMBEDDED_SYSTEM")
 class EmbeddedSystem extends RepresentationModel<EmbeddedSystem> implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private String name;
    private Double gyroscopeSensor;
    private Double accelerometerSensor;
    private String gpsPosition;
    private Date dataCollectionTime;

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getGyroscopeSensor() {
        return gyroscopeSensor;
    }

    public void setGyroscopeSensor(Double gyroscopeSensor) {
        this.gyroscopeSensor = gyroscopeSensor;
    }

    public Double getAccelerometerSensor() {
        return accelerometerSensor;
    }

    public void setAccelerometerSensor(Double accelerometerSensor) {
        this.accelerometerSensor = accelerometerSensor;
    }

    public String getGpsPosition() {
        return gpsPosition;
    }

    public void setGpsPosition(String gpsPosition) {
        this.gpsPosition = gpsPosition;
    }

    public Date getDataCollectionTime() {
        return dataCollectionTime;
    }

    public void setDataCollectionTime(Date dataCollectionTime) {
        this.dataCollectionTime = dataCollectionTime;
    }

}
