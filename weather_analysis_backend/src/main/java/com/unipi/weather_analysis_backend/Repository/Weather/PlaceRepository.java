package com.unipi.weather_analysis_backend.Repository.Weather;

import com.unipi.weather_analysis_backend.model.entity.Weather.IdClass.PlaceId;
import com.unipi.weather_analysis_backend.model.entity.Weather.Place;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;


@Transactional
public interface PlaceRepository extends JpaRepository<Place, PlaceId> {


}
