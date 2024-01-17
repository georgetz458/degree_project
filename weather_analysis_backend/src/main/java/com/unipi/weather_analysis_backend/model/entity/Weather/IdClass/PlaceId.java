package com.unipi.weather_analysis_backend.model.entity.Weather.IdClass;

import java.io.Serializable;
//για το συνδυασμό lon, lat ως πρωτεύων κλειδί
public class PlaceId implements Serializable {
    private double lon;
    private double lat;

    public PlaceId(double lon, double lat) {
        this.lon = lon;
        this.lat = lat;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    public PlaceId() {
    }
}
