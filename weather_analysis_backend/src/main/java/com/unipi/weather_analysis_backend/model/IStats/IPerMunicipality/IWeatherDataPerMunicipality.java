package com.unipi.weather_analysis_backend.model.IStats.IPerMunicipality;

public interface IWeatherDataPerMunicipality {
    Long getDate();
    Double getTemperature();
    Double getTempMax();
    Double getTempMin();
    Double getTempDiff();
    Double getHumidityPercentage();
    Double getAirPressure();
    Double getWindSpeed();
}
