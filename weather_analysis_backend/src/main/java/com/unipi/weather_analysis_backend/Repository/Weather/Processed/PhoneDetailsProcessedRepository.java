package com.unipi.weather_analysis_backend.Repository.Weather.Processed;

import com.unipi.weather_analysis_backend.model.IStats.IInOutPocketPerTempEstDiff;
import com.unipi.weather_analysis_backend.model.entity.Weather.Processed.PhoneDetailsProcessed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PhoneDetailsProcessedRepository extends JpaRepository<PhoneDetailsProcessed, Long> {
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
        FROM weather_processed AS w
        JOIN phone_details_processed AS p ON w.timestamp = p.timestamp AND w.municipality = p.municipality
        GROUP BY temperatureRange
        ) q1
    ORDER BY
        CASE
            WHEN temperatureRange = 'T <= 5' THEN 1
            WHEN temperatureRange = '5 < T <= 10' THEN 2
            WHEN temperatureRange = 'T > 10' THEN 3
        END;
    """, nativeQuery = true)
    List<IInOutPocketPerTempEstDiff> getInOutPocketPerTempEstDiff();
}
