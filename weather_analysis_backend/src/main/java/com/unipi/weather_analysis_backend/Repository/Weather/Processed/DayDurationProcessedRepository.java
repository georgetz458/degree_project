package com.unipi.weather_analysis_backend.Repository.Weather.Processed;

import com.unipi.weather_analysis_backend.model.IStats.IDayDuration;
import com.unipi.weather_analysis_backend.model.entity.Weather.Processed.DayDurationProcessed;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
@Transactional
public interface DayDurationProcessedRepository extends JpaRepository<DayDurationProcessed, Long> {
    @Query(value = """
                SELECT
                avg(sunrise) - extract(epoch from  date_trunc('day', to_timestamp(avg(timestamp)/1000))) as sunrise,
                avg(sunset) - extract(epoch from  date_trunc('day', to_timestamp(avg(timestamp)/1000))) as sunset,
                cast(to_timestamp(timestamp/1000) as date) as date
                FROM
                day_duration_processed
                group by date
                order by date;""", nativeQuery = true)
    List<IDayDuration> getSunriseAndSunset();
}
