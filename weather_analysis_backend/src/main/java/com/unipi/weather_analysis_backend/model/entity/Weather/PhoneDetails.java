package com.unipi.weather_analysis_backend.model.entity.Weather;

import jakarta.persistence.*;

@Entity
public class PhoneDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private double proximity;
    private String deviceManufacturer;
    private boolean inPocket;

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "lat", referencedColumnName = "lat", nullable = false),
            @JoinColumn(name = "lon", referencedColumnName = "lon", nullable = false)
    })
    private Place place;


    @ManyToOne
    @JoinColumn(name = "weatherTime", nullable = false)
    private DayDuration dayDuration;

    public PhoneDetails() {
    }

    public PhoneDetails( double proximity, String deviceManufacturer, boolean inPocket) {

        this.proximity = proximity;
        this.deviceManufacturer = deviceManufacturer;
        this.inPocket = inPocket;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getProximity() {
        return proximity;
    }

    public void setProximity(double proximity) {
        this.proximity = proximity;
    }

    public String getDeviceManufacturer() {
        return deviceManufacturer;
    }

    public void setDeviceManufacturer(String deviceManufacturer) {
        this.deviceManufacturer = deviceManufacturer;
    }

    public boolean isInPocket() {
        return inPocket;
    }

    public void setInPocket(boolean inPocket) {
        this.inPocket = inPocket;
    }

    public Place getPlace() {
        return place;
    }

    public void setPlace(Place place) {
        this.place = place;
    }

    public DayDuration getDayDuration() {
        return dayDuration;
    }

    public void setDayDuration(DayDuration dayDuration) {
        this.dayDuration = dayDuration;
    }
}
