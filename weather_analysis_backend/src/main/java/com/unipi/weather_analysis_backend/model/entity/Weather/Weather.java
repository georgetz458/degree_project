package com.unipi.weather_analysis_backend.model.entity.Weather;

import jakarta.persistence.*;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(name = "UniquePlaceAndTime",columnNames = {"lon", "lat", "weatherTime"})})
public class Weather {
    //PK(lon, lat, timestamp)
    //FK(lon,lat)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private double temperature;
    private double phoneTemp;


    private String weatherGeneral;


    //foreign keys

    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "lat", referencedColumnName = "lat", nullable = false),
            @JoinColumn(name = "lon", referencedColumnName = "lon", nullable = false)
    })
    private Place place;


    @ManyToOne
    @JoinColumn(name = "weatherTime", nullable = false)
    private DayDuration dayDuration;



    //@OneToMany(mappedBy = "weatherDetails")
    //private Set<Weather> weather;


    public Weather( double temperature, double phoneTemp, String weatherGeneral) {

        this.temperature = temperature;
        this.phoneTemp = phoneTemp;
        this.weatherGeneral = weatherGeneral;
    }

    public Weather() {

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temp) {
        this.temperature = temp;
    }

    public double getPhoneTemp() {
        return phoneTemp;
    }

    public void setPhoneTemp(double batteryTemp) {
        this.phoneTemp = batteryTemp;
    }




    public String getWeatherGeneral() {
        return weatherGeneral;
    }

    public void setWeatherGeneral(String weatherGeneral) {
        this.weatherGeneral = weatherGeneral;
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


    //public Set<Weather> getWeather() {
    //    return weather;
    //}

    //public void setWeather(Set<Weather> weather) {
    //    this.weather = weather;
    //}
}
