package com.unipi.weather_analysis_backend.model.entity.Weather.Processed;

import com.unipi.weather_analysis_backend.model.entity.Municipality;
import jakarta.persistence.*;

@Entity
public class WeatherProcessed {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private double temperature;
    private double phoneTemp;


    private String weatherGeneral;


    //foreign keys

    @ManyToOne
    @JoinColumn(name = "municipality", referencedColumnName = "name", nullable = false)
    private Municipality municipality;


    @ManyToOne
    @JoinColumn(name = "timestamp", nullable = false)
    private DayDurationProcessed dayDurationProcessed;



    //@OneToMany(mappedBy = "weatherDetails")
    //private Set<Weather> weather;


    public WeatherProcessed( double temperature, double phoneTemp, String weatherGeneral) {

        this.temperature = temperature;
        this.phoneTemp = phoneTemp;
        this.weatherGeneral = weatherGeneral;
    }

    public WeatherProcessed() {

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


    public Municipality getMunicipality() {
        return municipality;
    }


}
