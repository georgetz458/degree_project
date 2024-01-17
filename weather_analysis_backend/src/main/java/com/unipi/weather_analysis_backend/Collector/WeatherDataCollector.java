package com.unipi.weather_analysis_backend.Collector;

import com.unipi.weather_analysis_backend.Repository.Weather.*;
import com.unipi.weather_analysis_backend.model.entity.Weather.*;
import org.json.JSONException;
import org.json.JSONObject;

public class WeatherDataCollector implements JSONDataCollector {

    private Weather weather;
    private WeatherDetails weatherDetails;
    private PhoneDetails phoneDetails;
    private DayDuration dayDuration;
    private Place place;
    //Repositories
    private final DayDurationRepository dayDurationRepository;
    private final PlaceRepository placeRepository;
    private final WeatherRepository weatherRepository;
    private final  WeatherDetailsRepository weatherDetailsRepository;
    private final PhoneDetailsRepository phoneDetailsRepository;
    private final JSONObject jsonObject;


    private double toCelsius(double tempK){
        return tempK  - 273.15;
    }
    public WeatherDataCollector(JSONObject jsonObject, DayDurationRepository dayDurationRepository, PlaceRepository placeRepository, WeatherRepository weatherRepository, WeatherDetailsRepository weatherDetailsRepository, PhoneDetailsRepository phoneDetailsRepository){
        this.dayDurationRepository = dayDurationRepository;
        this.placeRepository = placeRepository;
        this.weatherRepository = weatherRepository;
        this.weatherDetailsRepository = weatherDetailsRepository;
        this.phoneDetailsRepository = phoneDetailsRepository;
        this.jsonObject = jsonObject;

    }
    //εισαγωγή δεδομένων σε entities
    public void setData(){
        try {

            //Αρχικοποίηση δεδομένων

            double phoneTemp = jsonObject.getDouble("phoneTemp");
            double light = jsonObject.getDouble("light");
            double proximity = jsonObject.getDouble("proximity");
            double phonePressure = jsonObject.getDouble("phonePressure");
            String deviceManufacturer = jsonObject.getString("deviceManufacturer");
            long timestamp = jsonObject.getLong("timestamp");
            boolean inPocket = jsonObject.getBoolean("inPocket");
            //από OpenWeatherData
            JSONObject owNameValuePairs = jsonObject.getJSONObject("openWeatherData").getJSONObject("nameValuePairs");

            int visibility = owNameValuePairs.getInt("visibility");
            String area = owNameValuePairs.getString("name");

            //από COORD
            JSONObject coordJSON = owNameValuePairs.getJSONObject("coord").getJSONObject("nameValuePairs");


            double lon = coordJSON.getDouble("lon");
            double lat = coordJSON.getDouble("lat");

            //από Weather
            JSONObject weatherJSON = owNameValuePairs.getJSONObject("weather")
                    .getJSONArray("values").getJSONObject(0).getJSONObject("nameValuePairs");
            String weatherGeneral = weatherJSON.getString("main");
            String weatherDescription = weatherJSON.getString("description");

            //από WeatherMain
            JSONObject weatherMainJSON = owNameValuePairs.getJSONObject("main").getJSONObject("nameValuePairs");
            //μετατροπή της μονάδας μέτρησης της θερμοκρασίας του OW από kelvin σε celsius
            double temp =toCelsius(weatherMainJSON .getDouble("temp"));
            double feelsLike = toCelsius(weatherMainJSON.getDouble("feels_like"));
            double tempMin = toCelsius(weatherMainJSON.getDouble("temp_min"));
            double tempMax = toCelsius(weatherMainJSON.getDouble("temp_max"));
            double OWPressure = weatherMainJSON.getDouble("pressure");
            int humidityPercentage = weatherMainJSON.getInt("humidity");

            //από Wind
            JSONObject windJSON = owNameValuePairs.getJSONObject("wind").getJSONObject("nameValuePairs");
            double windSpeed = windJSON.getDouble("speed");
            int windDeg = windJSON.getInt("deg");

            //από clouds
            int cloudiness = owNameValuePairs.getJSONObject("clouds").getJSONObject("nameValuePairs").getInt("all");

            //από sys
            JSONObject sysJSON = owNameValuePairs.getJSONObject("sys").getJSONObject("nameValuePairs");
            String country = sysJSON.getString("country");
            long sunrise = sysJSON.getLong("sunrise");
            long sunset = sysJSON.getLong("sunset");

            System.out.println("lon is: "+lon);

            //Αρχικοποίηση entities
            dayDuration = new DayDuration( timestamp, sunrise, sunset);
            place = new Place(  lon, lat, area, country);
            weather = new Weather(temp, phoneTemp, weatherGeneral);
            weatherDetails = new WeatherDetails(feelsLike, tempMin, tempMax, phonePressure, OWPressure, light, visibility, weatherDescription, humidityPercentage, cloudiness, windSpeed, windDeg);
            phoneDetails = new PhoneDetails(proximity, deviceManufacturer, inPocket);





        }catch (JSONException e){

            System.out.println(e.getMessage());
        }
    }
    //αποθήκευση entities
    public void save(){
        dayDurationRepository.save(dayDuration);
        placeRepository.save(place);
        weather.setPlace(place);
        weather.setDayDuration(dayDuration);
        weatherRepository.save(weather);


        weatherDetails.setPlace(place);
        weatherDetails.setDayDuration(dayDuration);

        phoneDetails.setPlace(place);
        phoneDetails.setDayDuration(dayDuration);

        weatherDetailsRepository.save(weatherDetails);
        phoneDetailsRepository.save(phoneDetails);

    }

}
