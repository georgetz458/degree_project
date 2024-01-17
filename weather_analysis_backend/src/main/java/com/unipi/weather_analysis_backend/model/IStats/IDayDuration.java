package com.unipi.weather_analysis_backend.model.IStats;

import java.util.Date;

public interface IDayDuration {
    Double getSunrise();
    Double getSunset();
    Date getDate();
}
