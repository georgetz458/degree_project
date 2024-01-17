package com.unipi.weather_analysis_backend.service;

import com.unipi.weather_analysis_backend.Repository.Weather.Processed.PlaceGeoRepository;
import com.unipi.weather_analysis_backend.Repository.Weather.WeatherRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
//service για τη δημιουργία PL/SQL συναρτήσεων και της προεπεξεργασίας των δεδομένων
@Service
public class DataAnalysisService {
    private final WeatherRepository weatherRepository;
    private final PlaceGeoRepository placeGeoRepository;

    @PersistenceContext
    private EntityManager entityManager;
    //true when starting data preProcessing
    public static boolean underMaintenance = false;

    @Autowired
    public DataAnalysisService(WeatherRepository weatherRepository, PlaceGeoRepository placeGeoRepository) {
        this.weatherRepository = weatherRepository;
        this.placeGeoRepository = placeGeoRepository;

    }
    //εκτελείται κατά την εκκίνηση της εφαρμογής
    //αρχικοποίηση PL/SQL συναρτήσεων μέσω Entity Manager
    @Transactional
    public void initializeFunctions() {


        String createFunctionGetAvgPerHour = """
                drop function if  exists get_avg_per_hour;
                CREATE FUNCTION get_avg_per_hour(numday numeric)
                RETURNS TABLE(hour time, temperature numeric, phoneTemp numeric, airPressure numeric, humidityPercentage numeric
                ,  weatherDescription varchar,  windSpeed numeric)
                AS $$
                DECLARE
                num_day numeric = numday;
                BEGIN
                RETURN QUERY
                EXECUTE format($sql$
                SELECT
                cast(to_timestamp(w.timestamp/1000) as time) AS hour,
                round(cast(avg(w.temperature) as numeric),2) as temperature,
                round(cast(avg(w.phone_temp) as numeric), 2) as phoneTemp,
                round(cast(avg(wd.owpressure) as numeric), 2) as airPressure,
                round(cast(avg(wd.humidity_percentage) as numeric), 2) as humidityPercentage,
                mode() WITHIN GROUP (ORDER BY wd.weather_description) AS weatherDescription,
                round(cast(avg(wd.wind_speed) as numeric), 2) as windSpeed
                FROM
                weather_processed as w join weather_details_processed as wd on w.id=wd.id
                WHERE cast(to_timestamp(w.timestamp/1000) as date) = (
                SELECT cast(((SELECT min(cast(to_timestamp(timestamp/1000) as date)) FROM weather_processed) + interval '1 day' * %s) as date)
                )
                GROUP BY hour
                ORDER BY hour
                $sql$, num_day);
                END;
                $$ LANGUAGE plpgsql;""";
        String createFunctionPerMunicipality =
                """
                        drop function if exists get_per_municipality;
                        create function get_per_municipality(municipalityName text)
                        returns table(date bigint, temperature numeric, tempMax numeric, tempMin numeric,
                        tempDiff numeric, humidityPercentage numeric,
                        airPressure numeric, windSpeed numeric)
                        AS $$
                        DECLARE
                        municipality_name text = municipalityName;
                        BEGIN
                        RETURN QUERY
                        EXECUTE format($sql$
                        select
                        w.timestamp as date,
                        round(cast(avg(w.temperature) as numeric), 2) as temperature,round(cast(avg(wd.temp_max) as numeric), 2) as tempMax,
                        round(cast(avg(wd.temp_min) as numeric), 2) as tempMin,
                        round(cast (abs(avg(w.temperature) - avg(w.phone_temp)) as numeric),2) as tempDiff,
                        round(cast(avg(wd.humidity_percentage) as numeric), 2) as humidityPercentage,
                        round(cast(avg(wd.owpressure) as numeric), 2) as airPressure,
                        round(cast(avg(wd.wind_speed) as numeric), 2) as windSpeed
                        from weather_processed w
                        join weather_details_processed wd on w.id = wd.id
                        join place_geo pg on  w.municipality = pg.municipality
                        where pg.municipality = '%s'
                        group by date
                        order by date
                        $sql$, municipality_name);
                        END;
                        $$ LANGUAGE plpgsql;""";

        entityManager.createNativeQuery(createFunctionGetAvgPerHour).executeUpdate();
        entityManager.createNativeQuery(createFunctionPerMunicipality).executeUpdate();
    }
    //εκτέλεση προεπεξεργασίας μέσω Entity Manager
    @Transactional
    public void startPreProcessing(){

        String deleteData = """
                ALTER SEQUENCE weather_processed_id_seq RESTART;
                ALTER SEQUENCE weather_details_processed_id_seq RESTART;
                ALTER SEQUENCE phone_details_processed_id_seq RESTART;
                delete from weather_details_processed;
                delete from weather_processed;
                delete from phone_details_processed;
                delete from day_duration_processed;
                delete from place_geo;""";

        String initPlaceGeo = """
                insert into place_geo (lon, lat) (select lon, lat from place);
                update place_geo set point=st_setsrid(st_makepoint(cast(lon as real), cast(lat as real)), 4326);
                update place_geo  set municipality= q.municipality from  (
                select  pg.lon as lon, pg.lat as lat, m.name as municipality
                from place_geo pg
                join municipality m on ST_CONTAINS(m.the_geom, pg.point)
                ) q
                where place_geo.lon = q.lon and place_geo.lat = q.lat;delete from place_geo
                where (point) in (
                (select point from place_geo)
                except
                (select  pg.point
                from place_geo pg
                join municipality m on ST_CONTAINS(m.the_geom, pg.point))
                );""";

        String initDayDurationProcessed = """
                insert into day_duration_processed
                (timestamp, sunset, sunrise)
                (select
                 cast(extract(epoch from date_trunc('hour', to_timestamp(weather_time/1000))) * 1000 as bigint) as timestamp,
                 cast(avg(sunset) as bigint) as sunset,
                 cast(avg(sunrise)as bigint) as sunrise
                 from day_duration
                 WHERE EXTRACT(hour FROM to_timestamp(weather_time / 1000)) BETWEEN 6 AND 22
                 group by timestamp
                 order by timestamp
                );""";
        String initWeatherProcessed= """
                insert into weather_processed
                (timestamp, temperature, phone_temp, weather_general, municipality)
                (select
                 cast(extract(epoch from date_trunc('hour', to_timestamp(w.weather_time/1000))) * 1000 as bigint) as timestamp,
                 round(cast(avg(w.temperature) as numeric),2) as temperature,
                 round(cast(avg(w.phone_temp) as numeric),2) as phone_temp,
                 mode() WITHIN GROUP (ORDER BY w.weather_general) AS weather_general,
                 pg.municipality as municipality
                 from
                 weather as w join place_geo as pg on w.lon = pg.lon and w.lat = pg.lat
                 WHERE EXTRACT(hour FROM to_timestamp(weather_time / 1000)) BETWEEN 6 AND 22
                 group by timestamp, pg.municipality
                 order by timestamp
                );""";
        String initWeatherDetailsProcessed = """
                insert into weather_details_processed
                (timestamp, owpressure, temp_min, temp_max, weather_description, humidity_percentage, wind_speed, wind_deg, municipality)
                (select
                 cast(extract(epoch from date_trunc('hour', to_timestamp(w.weather_time/1000))) * 1000 as bigint) as timestamp,
                 round(cast(avg(w.owpressure) as numeric),2) as owpressure,
                 round(cast(avg(w.temp_min) as numeric),2) as temp_min,
                 round(cast(avg(w.temp_max) as numeric),2) as temp_max,
                 mode() WITHIN GROUP (ORDER BY w.weather_description) AS weather_description,
                 round(cast(avg(w.humidity_percentage) as numeric),2) as humidity_percentage,
				 round(cast(avg(w.wind_speed) as numeric),2) as wind_speed,
				 round(cast(avg(w.wind_deg) as numeric),2) as wind_deg,
                 pg.municipality as municipality
                 from
                 weather_details as w join place_geo as pg on w.lon = pg.lon and w.lat = pg.lat
                 WHERE EXTRACT(hour FROM to_timestamp(weather_time / 1000)) BETWEEN 6 AND 22
                 group by timestamp, pg.municipality
                 order by timestamp
                );""";
        String initPhoneDetailsProcessed = """
                insert into phone_details_processed
                (timestamp, in_pocket, municipality)
                (select
                cast(extract(epoch from date_trunc('hour', to_timestamp(p.weather_time/1000))) * 1000 as bigint) as timestamp,
                mode() WITHIN GROUP (ORDER BY p.in_pocket) AS in_pocket,
                pg.municipality as municipality
                from
                phone_details as p join place_geo as pg on p.lon = pg.lon and p.lat = pg.lat
                WHERE EXTRACT(hour FROM to_timestamp(weather_time / 1000)) BETWEEN 6 AND 22
                group by timestamp, pg.municipality
                order by timestamp
                );""";



        entityManager.createNativeQuery(deleteData+initPlaceGeo+initDayDurationProcessed+initWeatherProcessed+initWeatherDetailsProcessed+initPhoneDetailsProcessed).executeUpdate();



    }


}
