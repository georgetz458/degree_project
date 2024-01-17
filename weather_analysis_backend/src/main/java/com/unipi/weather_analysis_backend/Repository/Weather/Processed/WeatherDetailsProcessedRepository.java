package com.unipi.weather_analysis_backend.Repository.Weather.Processed;

import com.unipi.weather_analysis_backend.model.IStats.IWindDirection;
import com.unipi.weather_analysis_backend.model.entity.Weather.Processed.WeatherDetailsProcessed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface WeatherDetailsProcessedRepository extends JpaRepository<WeatherDetailsProcessed, Long> {
    @Query(value = """
            SELECT COALESCE(count(d.wind_deg), 0) AS count
            FROM (
             SELECT generate_series(0, 360) AS wind_deg
            ) AS w
            LEFT JOIN (
                select
                cast(avg(wind_deg) as bigint) as wind_deg from weather_details_processed group by timestamp order by timestamp
            )  d
            ON d.wind_deg = w.wind_deg
            GROUP BY w.wind_deg
            ORDER BY w.wind_deg;
            """, nativeQuery = true)
    List<IWindDirection> getWindDirection();
}
