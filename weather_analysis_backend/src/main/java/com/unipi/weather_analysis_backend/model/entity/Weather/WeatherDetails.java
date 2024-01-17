package com.unipi.weather_analysis_backend.model.entity.Weather;

import jakarta.persistence.*;

@Entity
public class WeatherDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private double feelsLike;
    private double tempMin;
    private double tempMax;
    private double phonePressure;
    private double OWPressure;
    private double light;
    private int visibility;
    private String weatherDescription;
    private int humidityPercentage;
    private int cloudiness;
    private double windSpeed;
    private double windDeg;//wind direction degrees

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "lat", referencedColumnName = "lat", nullable = false),
            @JoinColumn(name = "lon", referencedColumnName = "lon", nullable = false)
    })
    private Place place;


    @ManyToOne
    @JoinColumn(name = "weatherTime", nullable = false)
    private DayDuration dayDuration;

    public WeatherDetails() {
    }

    public WeatherDetails(double feelsLike, double tempMin, double tempMax, double phonePressure, double OWPressure, double light, int visibility, String weatherDescription, int humidityPercentage, int cloudiness, double windSpeed, double windDeg) {

        this.feelsLike = feelsLike;
        this.tempMin = tempMin;
        this.tempMax = tempMax;
        this.phonePressure = phonePressure;
        this.OWPressure = OWPressure;
        this.light = light;
        this.visibility = visibility;
        this.weatherDescription = weatherDescription;
        this.humidityPercentage = humidityPercentage;
        this.cloudiness = cloudiness;
        this.windSpeed = windSpeed;
        this.windDeg = windDeg;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getFeelsLike() {
        return feelsLike;
    }

    public void setFeelsLike(double feelsLike) {
        this.feelsLike = feelsLike;
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

    public double getPhonePressure() {
        return phonePressure;
    }

    public void setPhonePressure(double phonePressure) {
        this.phonePressure = phonePressure;
    }

    public double getOWPressure() {
        return OWPressure;
    }

    public void setOWPressure(double OWPressure) {
        this.OWPressure = OWPressure;
    }

    public double getLight() {
        return light;
    }

    public void setLight(double light) {
        this.light = light;
    }

    public int getVisibility() {
        return visibility;
    }

    public void setVisibility(int visibility) {
        this.visibility = visibility;
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

    public int getCloudiness() {
        return cloudiness;
    }

    public void setCloudiness(int cloudiness) {
        this.cloudiness = cloudiness;
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
