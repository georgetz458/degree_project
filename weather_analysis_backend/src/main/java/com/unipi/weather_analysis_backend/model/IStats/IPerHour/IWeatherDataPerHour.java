package com.unipi.weather_analysis_backend.model.IStats.IPerHour;

import java.time.LocalTime;

public interface IWeatherDataPerHour {
    LocalTime getHour();
    Double getTemperature();
    Double getPhoneTemp();
    Double getAirPressure();
    Double getHumidityPercentage();
    String getWeatherDescription();
    Double getWindSpeed();
}
