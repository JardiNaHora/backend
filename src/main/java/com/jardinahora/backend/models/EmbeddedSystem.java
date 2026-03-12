package com.jardinahora.backend.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.hateoas.RepresentationModel;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "TB_EMBEDDED_SYSTEM")
public class EmbeddedSystem extends RepresentationModel<EmbeddedSystem> implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @Column(name = "system_name")
    private String name;
    
    @Column(name = "gyroscope_sensor")
    private Double gyroscopeSensor;
    
    @Column(name = "accelerometer_sensor")
    private Double accelerometerSensor;
    
    @Column(name = "gps_position", length = 100)
    private String gpsPosition;
    
    @Column(name = "data_collection_time")
    @NotNull
    private Date dataCollectionTime;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vehicle_id", nullable = false)
    @NotNull
    private Vehicle vehicle;

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

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

}
