package com.unipi.weather_analysis_backend.controller;

import com.unipi.weather_analysis_backend.MyErrorHandler;
import com.unipi.weather_analysis_backend.Repository.Weather.*;
import com.unipi.weather_analysis_backend.Collector.WeatherDataCollector;
import org.json.JSONObject;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
public class MainController {


    private final DayDurationRepository dayDurationRepository;

    private final PlaceRepository placeRepository;
    private  final WeatherDetailsRepository weatherDetailsRepository;
    private  final PhoneDetailsRepository phoneDetailsRepository;



    private final WeatherRepository weatherRepository;

    public MainController(DayDurationRepository dayDurationRepository, PlaceRepository placeRepository, WeatherRepository weatherRepository, WeatherDetailsRepository weatherDetailsRepository, PhoneDetailsRepository phoneDetailsRepository){
        this.dayDurationRepository = dayDurationRepository;
        this.placeRepository = placeRepository;
        this.weatherRepository = weatherRepository;
        this.weatherDetailsRepository = weatherDetailsRepository;
        this.phoneDetailsRepository = phoneDetailsRepository;

    }
    //μεταφορά δεδομένων από το JSON του request στη βάση
    @PostMapping("/collect")
    public ResponseEntity<Map<String, Object>> collect(@RequestBody Map<String, Object> json){
        System.out.println("Map: "+json);

        JSONObject  jsonObject = new JSONObject(json);

        WeatherDataCollector weatherDataCollector = new WeatherDataCollector(jsonObject, dayDurationRepository, placeRepository, weatherRepository, weatherDetailsRepository, phoneDetailsRepository);
        weatherDataCollector.setData();
        JSONObject response = new JSONObject();
        response.put("Result", "Success");
        try {
            weatherDataCollector.save();

            return ResponseEntity.status(HttpStatus.OK).body(response.toMap());
        }catch (Exception e){
            //εάν χρήσης τύχει να έχει ίδια τοποθεσία και να στείλει την ίδια χρονική στιγμή request
            //τότε  για την αποφυγή duplicates δεν αποθηκεύεται
            if(e instanceof  DataIntegrityViolationException){
                return ResponseEntity.status(HttpStatus.OK).body(response.toMap());
            }
            else return MyErrorHandler.handleException(e);
        }

    }

}
