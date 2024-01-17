package com.unipi.weather_analysis_backend.Repository.Weather;

import com.unipi.weather_analysis_backend.model.entity.Weather.DayDuration;
import org.springframework.data.jpa.repository.JpaRepository;


public interface DayDurationRepository extends JpaRepository<DayDuration, Long> {

}
