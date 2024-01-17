package com.unipi.weather_analysis_backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

    @GetMapping("/loginPage")
    public String loginPage(){
        return "loginPage";
    }
}
