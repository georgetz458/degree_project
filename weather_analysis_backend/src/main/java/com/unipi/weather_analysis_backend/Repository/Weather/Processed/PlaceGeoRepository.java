package com.unipi.weather_analysis_backend.Repository.Weather.Processed;

import com.unipi.weather_analysis_backend.model.IStats.IGeomText;
import com.unipi.weather_analysis_backend.model.IStats.IPerMunicipality.IWeatherDataPerMunicipality;
import com.unipi.weather_analysis_backend.model.entity.Weather.IdClass.PlaceId;
import com.unipi.weather_analysis_backend.model.entity.Weather.Processed.PlaceGeo;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Transactional
public interface PlaceGeoRepository extends JpaRepository<PlaceGeo, PlaceId> {

    @Query(value = """
            SELECT ST_AsText(m.the_geom) as geomText, m.name\s
            FROM ( SELECT * FROM place_geo) coord\s
            JOIN municipality m ON ST_Contains(m.the_geom, coord.point)
            ;""", nativeQuery = true)
    List<IGeomText> getMunicipalities();

    @Query(value = "Select * from get_per_municipality(:municipality);", nativeQuery = true)
    List<IWeatherDataPerMunicipality> getWeatherDataPerMunicipality(@Param("municipality") String municipality);
}
