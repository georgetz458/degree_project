package com.unipi.weather_analysis_backend.model.entity.Weather;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
public class DayDuration {
    @Id
    private long weatherTime;
    private long sunrise;
    private long sunset;
    @OneToMany(mappedBy = "dayDuration")
    private Set<Weather> weathers = new HashSet<>();



    public DayDuration(long weatherTime, long sunrise, long sunset, Set<Weather> weathers) {
        this.weatherTime = weatherTime;
        this.sunrise = sunrise;
        this.sunset = sunset;
        this.weathers = weathers;

    }

    public DayDuration(long weatherTime, long sunrise, long sunset) {
        this.weatherTime = weatherTime;
        this.sunrise = sunrise;
        this.sunset = sunset;
    }

    public DayDuration() {

    }

    public long getWeatherTime() {
        return weatherTime;
    }

    public void setWeatherTime(long timestamp) {
        this.weatherTime = timestamp;
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

    public Set<Weather> getWeathers() {
        return weathers;
    }

    public void setWeatherDetails(Set<Weather> weathers) {
        this.weathers = weathers;
        weathers.forEach(weatherDetail -> weatherDetail.setDayDuration(this));
    }

}
