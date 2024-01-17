package com.unipi.weather_analysis_backend.model.entity.Weather.Processed;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import java.util.HashSet;
import java.util.Set;

@Entity
public class DayDurationProcessed {
    @Id
    private long timestamp;
    private long sunrise;
    private long sunset;
    @OneToMany(mappedBy = "dayDurationProcessed")
    private Set<WeatherProcessed> weatherProcessedSet = new HashSet<>();
    @OneToMany(mappedBy = "dayDurationProcessed")
    private Set<WeatherDetailsProcessed> weatherDetailsProcessedSet = new HashSet<>();



    public DayDurationProcessed() {

    }



    public long getSunrise() {
        return sunrise;
    }

    public void setSunrise(long sunrise) {
        this.sunrise = sunrise;
    }

    public long getSunset() {
        return sunset;
    }

    public void setSunset(long sunset) {
        this.sunset = sunset;
    }

    public Set<WeatherProcessed> getWeathers() {
        return weatherProcessedSet;
    }


}
