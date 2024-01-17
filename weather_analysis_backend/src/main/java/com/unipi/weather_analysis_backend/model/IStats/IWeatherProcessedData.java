package com.unipi.weather_analysis_backend.model.IStats;

public interface IWeatherProcessedData {
    Long getTimestamp();
    Double getTemperature();
    Double getPhoneTemp();
    Double getTempMin();
    Double getTempMax();
    Double getAirPressure();
    Double getHumidityPercentage();
    Double getWindSpeed();
    String getWeatherDescription();
}
