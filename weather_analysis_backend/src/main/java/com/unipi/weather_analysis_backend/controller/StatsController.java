package com.unipi.weather_analysis_backend.controller;

import com.unipi.weather_analysis_backend.Repository.Weather.*;
import com.unipi.weather_analysis_backend.Repository.Weather.Processed.*;
import com.unipi.weather_analysis_backend.model.IStats.*;
import com.unipi.weather_analysis_backend.model.IStats.IPerDay.IWeatherDataPerDay;
import com.unipi.weather_analysis_backend.model.IStats.IPerHour.IWeatherDataPerHour;
import com.unipi.weather_analysis_backend.model.IStats.IPerMunicipality.IWeatherDataPerMunicipality;
import com.unipi.weather_analysis_backend.service.DataAnalysisService;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;


@Controller
public class StatsController {
    private final WeatherRepository weatherRepository;
    private final WeatherDetailsProcessedRepository weatherDetailsProcessedRepository;
    private final PlaceGeoRepository placeGeoRepository;

    private final WeatherProcessedRepository weatherProcessedRepository;
    private final DayDurationProcessedRepository dayDurationProcessedRepository;
    private final PhoneDetailsRepository phoneDetailsRepository;
    private final PhoneDetailsProcessedRepository phoneDetailsProcessedRepository;

    private final DataAnalysisService dataAnalysisService;

    public StatsController(WeatherRepository weatherRepository, WeatherDetailsProcessedRepository weatherDetailsProcessedRepository, PlaceGeoRepository placeGeoRepository, WeatherProcessedRepository weatherProcessedRepository, DayDurationProcessedRepository dayDurationProcessedRepository, PhoneDetailsRepository phoneDetailsRepository, PhoneDetailsProcessedRepository phoneDetailsProcessedRepository, DataAnalysisService dataAnalysisService) {
        this.weatherRepository = weatherRepository;
        this.weatherDetailsProcessedRepository = weatherDetailsProcessedRepository;
        this.placeGeoRepository = placeGeoRepository;
        this.weatherProcessedRepository = weatherProcessedRepository;
        this.dayDurationProcessedRepository = dayDurationProcessedRepository;
        this.phoneDetailsRepository = phoneDetailsRepository;
        this.phoneDetailsProcessedRepository = phoneDetailsProcessedRepository;
        this.dataAnalysisService = dataAnalysisService;
    }

    //σελίδα στατιστικών ανά ώρα
    @GetMapping("/avgPerHour")
    public String avgPerHour(@RequestParam(name = "day", required = false, defaultValue = "0") int day, Model model){

        //List<IWeatherDataPerHour> weatherDataPerHourList = weatherRepository.getWeatherDataPerHour(day);
        List<IWeatherDataPerHour> weatherDataPerHourList = weatherProcessedRepository.getWeatherDataPerHour(day);
        model.addAttribute("weatherDataPerHourList", weatherDataPerHourList);
        return "avgPerHour";
    }
    //σελίδα κύριων στατιστικών
    @GetMapping("/mainStats")
    public String avgPerDay(Model model){


        List<IInOutPocketPerTempEstDiff> inOutPocketPerTempEstDiffList = phoneDetailsProcessedRepository.getInOutPocketPerTempEstDiff();
        List<IWeatherProcessedData> weatherProcessedDataList = weatherProcessedRepository.getWeatherProcessedData();
        List<IWindDirection> windDirectionList = weatherDetailsProcessedRepository.getWindDirection();
        List<IDayDuration> dayDurations = dayDurationProcessedRepository.getSunriseAndSunset();
        List<IWeatherGeneralOccurrence> weatherGeneralOccurrences = weatherProcessedRepository.getWeatherGeneralOccurrence();
        List<IWeatherDataPerDay> weatherDataPerDayList = weatherProcessedRepository.getWeatherDataPerDay();


        model.addAttribute("weatherProcessedDataList", weatherProcessedDataList);
        model.addAttribute("windDirectionList", windDirectionList);
        model.addAttribute("inOutPocketPerTempEstDiffList", inOutPocketPerTempEstDiffList);
        model.addAttribute("dayDurations", dayDurations);
        model.addAttribute("weatherGeneralOccurrences", weatherGeneralOccurrences);
        model.addAttribute("weatherDataPerDayList", weatherDataPerDayList);
        return "mainStats";
    }
    //σελίδα χάρτη
    @GetMapping("weatherMap")
    public String weatherMap(Model model){

        List<IGeomText> municipalities = placeGeoRepository.getMunicipalities();

        model.addAttribute("mode", "temperature");
        model.addAttribute("municipalities", municipalities);
        System.out.println("~!~"+municipalities.get(0).getGeomText());
        return "weatherMap";
    }
    //σελίδα στατιστικών ανά δήμο
    @GetMapping("/perMunicipality")
    public String perMunicipality(@RequestParam(name = "municipality") String municipality, Model model){
        List<IWeatherDataPerMunicipality> weatherDataPerMunicipalityList = placeGeoRepository.getWeatherDataPerMunicipality(municipality);

        model.addAttribute("weatherDataPerMunicipalityList", weatherDataPerMunicipalityList);
        return "perMunicipality";
    }
    //προεπεξεργασία δεδομένων
    @GetMapping("/startDataPreprocessing")
    public ResponseEntity<Map<String, Object>> startDataPreprocessing() throws InterruptedException {
        //TODO add data analysis process
        dataAnalysisService.startPreProcessing();
        JSONObject response = new JSONObject();
        response.put("Result", "Success");
        return ResponseEntity.status(HttpStatus.OK).body(response.toMap());
    }
    //κεντρική σελίδα
    @GetMapping("/")
    public String intro(){
        return "intro";
    }
}
