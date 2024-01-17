package com.unipi.weather_analysis_backend.Repository.Weather;

import com.unipi.weather_analysis_backend.model.entity.Weather.WeatherDetails;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

@Transactional
public interface WeatherDetailsRepository extends JpaRepository<WeatherDetails, Long> {

}
