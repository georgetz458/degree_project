package com.unipi.weather_analysis_backend.model.entity.Weather.Processed;

import com.unipi.weather_analysis_backend.model.entity.Municipality;
import jakarta.persistence.*;

@Entity
public class WeatherDetailsProcessed {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;


    private double tempMin;
    private double tempMax;

    private double OWPressure;

    private String weatherDescription;
    private int humidityPercentage;

    private double windSpeed;
    private double windDeg;//wind direction degrees

    @ManyToOne
    @JoinColumn(name = "municipality", referencedColumnName = "name", nullable = false)
    private Municipality municipality;


    @ManyToOne
    @JoinColumn(name = "timestamp", nullable = false)
    private DayDurationProcessed dayDurationProcessed;

    public WeatherDetailsProcessed() {
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }



    public double getTempMin() {
        return tempMin;
    }

    public void setTempMin(double tempMin) {
        this.tempMin = tempMin;
    }

    public double getTempMax() {
        return tempMax;
    }

    public void setTempMax(double tempMax) {
        this.tempMax = tempMax;
    }



    public double getOWPressure() {
        return OWPressure;
    }

    public void setOWPressure(double OWPressure) {
        this.OWPressure = OWPressure;
    }


    public String getWeatherDescription() {
        return weatherDescription;
    }

    public void setWeatherDescription(String weatherDescription) {
        this.weatherDescription = weatherDescription;
    }

    public int getHumidityPercentage() {
        return humidityPercentage;
    }

    public void setHumidityPercentage(int humidityPercentage) {
        this.humidityPercentage = humidityPercentage;
    }


    public double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public double getWindDeg() {
        return windDeg;
    }

    public void setWindDeg(double windDeg) {
        this.windDeg = windDeg;
    }

    public Municipality getMunicipality() {
        return municipality;
    }


}
