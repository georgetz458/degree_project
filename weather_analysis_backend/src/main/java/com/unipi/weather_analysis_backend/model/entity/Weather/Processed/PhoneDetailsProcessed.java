package com.unipi.weather_analysis_backend.model.entity.Weather.Processed;

import com.unipi.weather_analysis_backend.model.entity.Municipality;
import jakarta.persistence.*;
@Entity
public class PhoneDetailsProcessed {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;


    private boolean inPocket;

    @ManyToOne
    @JoinColumn(name = "municipality", referencedColumnName = "name", nullable = false)
    private Municipality municipality;


    @ManyToOne
    @JoinColumn(name = "timestamp", nullable = false)
    private DayDurationProcessed dayDurationProcessed;

    public PhoneDetailsProcessed() {
    }

    public PhoneDetailsProcessed( double proximity, String deviceManufacturer, boolean inPocket) {


        this.inPocket = inPocket;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }



    public boolean isInPocket() {
        return inPocket;
    }

    public void setInPocket(boolean inPocket) {
        this.inPocket = inPocket;
    }

    public Municipality getMunicipality() {
        return municipality;
    }

    public DayDurationProcessed getDayDurationProcessed() {
        return dayDurationProcessed;
    }
}
