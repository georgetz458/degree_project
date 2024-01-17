package com.unipi.weather_analysis_backend.model.IStats.IPerDay;

import java.util.Date;

public interface IWeatherDataPerDay {
    Date getDate();
    Double getTemperature();
    Double getPhoneTemp();
    Double getAirPressure();
    Double getHumidityPercentage();
    String getWeatherDescription();
    Double getWindSpeed();
}
