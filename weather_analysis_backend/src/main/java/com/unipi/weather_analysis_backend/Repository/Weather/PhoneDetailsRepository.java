package com.unipi.weather_analysis_backend.Repository.Weather;

import com.unipi.weather_analysis_backend.model.IStats.IInOutPocketPerTempEstDiff;
import com.unipi.weather_analysis_backend.model.entity.Weather.PhoneDetails;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

@Transactional
public interface PhoneDetailsRepository extends JpaRepository<PhoneDetails, Long> {

    @Query(value = """
SELECT
        temperatureRange,
        (inPocket * 100 / total) AS inPocket,
        (outOfPocket * 100 / total) AS outOfPocket
    FROM
        (
        SELECT
            CASE WHEN ABS(phone_temp - temperature) <= 5 THEN 'T <= 5'
                 WHEN ABS(phone_temp - temperature) > 5 AND ABS(phone_temp - temperature) <= 10 THEN '5 < T <= 10'
                 WHEN ABS(phone_temp - temperature) > 10 THEN 'T > 10'
            END AS temperatureRange,
            Count(in_pocket) AS total,
            COUNT(CASE WHEN in_pocket = false THEN 1 END) AS outOfPocket,
            COUNT(CASE WHEN in_pocket = true THEN 1 END) AS inPocket
        FROM weather AS w
        JOIN phone_details AS p ON w.weather_time = p.weather_time AND w.lat = p.lat AND w.lon = p.lon
        GROUP BY temperatureRange
        ) q1
    ORDER BY
        CASE\s
            WHEN temperatureRange = 'T <= 5' THEN 1
            WHEN temperatureRange = '5 < T <= 10' THEN 2
            WHEN temperatureRange = 'T > 10' THEN 3
        END;
    """, nativeQuery = true)
    List<IInOutPocketPerTempEstDiff> getInOutPocketPerTempEstDiff();
}
