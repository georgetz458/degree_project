package com.unipi.weather_analysis_backend.model.entity.Weather;

import com.unipi.weather_analysis_backend.model.entity.Weather.IdClass.PlaceId;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@IdClass(PlaceId.class)
public class Place {
    @Id
    private double lon;
    @Id
    private double lat;

    private String area;
    private String country;
    @OneToMany(mappedBy = "place")
    private Set<Weather> weathers =  new HashSet<>();





    public Place(double lon, double lat, String area, String country, Set<Weather> weathers) {
        this.lon = lon;
        this.lat = lat;
        this.area = area;
        this.country = country;
        this.weathers = weathers;

    }

    public Place(double lon, double lat, String area, String country) {
        this.lon = lon;
        this.lat = lat;
        this.area = area;
        this.country = country;
    }

    public Place() {

    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Set<Weather> getWeathers() {
        return weathers;
    }

    public void setWeatherDetails(Set<Weather> weathers) {
        this.weathers = weathers;
        weathers.forEach(weather -> weather.setPlace(this));
    }


}
