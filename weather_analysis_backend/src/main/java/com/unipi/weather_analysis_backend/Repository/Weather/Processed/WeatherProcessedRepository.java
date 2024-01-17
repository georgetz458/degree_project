package com.unipi.weather_analysis_backend.Repository.Weather.Processed;

import com.unipi.weather_analysis_backend.model.IStats.IPerDay.IWeatherDataPerDay;
import com.unipi.weather_analysis_backend.model.IStats.IPerHour.IWeatherDataPerHour;
import com.unipi.weather_analysis_backend.model.IStats.IWeatherGeneralOccurrence;
import com.unipi.weather_analysis_backend.model.IStats.IWeatherProcessedData;
import com.unipi.weather_analysis_backend.model.entity.Weather.Processed.WeatherProcessed;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
@Transactional
public interface WeatherProcessedRepository extends JpaRepository<WeatherProcessed, Long> {

    @Query(value = """
            select
            w.timestamp,
            round(cast(avg(w.temperature) as numeric), 2) as temperature, round(cast(avg(w.phone_temp) as numeric), 2) as phoneTemp,
            round(cast(avg(wd.temp_min) as numeric), 2) as tempMin,round(cast(avg(wd.temp_max) as numeric), 2) as tempMax,
            round(cast(avg(wd.owpressure) as numeric), 2) as airPressure,
            round(cast(avg(wd.humidity_percentage) as numeric), 2) as humidityPercentage,
            round(cast(avg(wd.wind_speed) as numeric), 2) as windSpeed,
            mode() WITHIN GROUP (ORDER BY wd.weather_description) as weatherDescription
            from weather_processed w
            join weather_details_processed wd
            on w.id= wd.id
            group by w.timestamp
            order by w.timestamp;
            """, nativeQuery = true)
    List<IWeatherProcessedData> getWeatherProcessedData();



    @Query(value = """
            --percentage of each weather_general
            SELECT
            weather_general AS weatherGeneral,
            COUNT(*) * 100.0 / (SELECT COUNT(*) FROM weather_processed) AS percentage
            FROM
            weather_processed
            GROUP BY
            weather_general;""", nativeQuery = true)
    List<IWeatherGeneralOccurrence> getWeatherGeneralOccurrence();

    @Query(value = """
            Select
            cast( to_timestamp(w.timestamp/1000) as date) as date,
            round(cast(avg(w.temperature) as numeric),2) as temperature,
            round(cast(avg(w.phone_temp) as numeric), 2) as phoneTemp,
            round(cast(avg(wd.owpressure) as numeric), 2) as airPressure,
            round(cast(avg(wd.humidity_percentage) as numeric), 2) as humidityPercentage,
            mode() WITHIN GROUP (ORDER BY wd.weather_description) AS weatherDescription,
            round(cast(avg(wd.wind_speed) as numeric), 2) as windSpeed
            From
            weather_details_processed as wd join weather_processed as w on wd.id = w.id
            Group by
             date
            Order by
            date;
            """, nativeQuery = true)
    List<IWeatherDataPerDay> getWeatherDataPerDay();

    @Query(value = "select * from get_avg_per_hour(:numDay)", nativeQuery = true)
    List<IWeatherDataPerHour> getWeatherDataPerHour(@Param("numDay") int numDay);
}
