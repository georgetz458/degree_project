package com.unipi.weather_analysis_backend;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.unipi.weather_analysis_backend.service.DataAnalysisService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@SpringBootApplication
public class WeatherAnalysisBackendApplication {
    @Autowired
    private DataAnalysisService dataAnalysisService;

    public static void main(String[] args) throws IOException {


        //για τη firebase
        ClassPathResource resource = new ClassPathResource("serviceAccountKey.json");
        InputStream serviceAccount = resource.getInputStream();
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        FirebaseApp.initializeApp(options);
        String classpath = System.getProperty("java.class.path");
        System.out.println(classpath);


        SpringApplication.run(WeatherAnalysisBackendApplication.class, args);
    }
    //αρχικοποίηση PL/SQL συναρτήσεων μέσω Entity Manager
    @PostConstruct
    public void initializeFunctions() {
        dataAnalysisService.initializeFunctions();
    }

}
