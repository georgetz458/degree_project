package com.unipi.weather_analysis_backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity

public class SpringSecurityConfig{





    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/register").permitAll()
                        .requestMatchers("/login").permitAll()
                        .requestMatchers("/collect").hasAuthority("SCOPE_USER")
                        .requestMatchers("/").permitAll()
                        .requestMatchers("/mainStats").permitAll()
                        .requestMatchers("/weatherMap").permitAll()
                        .requestMatchers("/avgPerHour").permitAll()
                        .requestMatchers("/perMunicipality").permitAll()
                        .requestMatchers("/startDataPreprocessing").hasAuthority("SCOPE_ADMIN")
                        .requestMatchers("/icons/**").permitAll()
                        .requestMatchers("/js/**").permitAll()
                        .requestMatchers("/css/**").permitAll()
                        .requestMatchers("/loginPage").permitAll()
                        .requestMatchers("/background.jpg").permitAll()
                        .requestMatchers("/favicon.ico").permitAll()
                        .anyRequest()
                        .authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt);

        return http.build();
    }

    @Bean
    JwtDecoder jwtDecoder() {
        String issuerUri = "https://securetoken.google.com/weatheranalysis-71e76";
        return JwtDecoders.fromIssuerLocation(issuerUri);
    }


}
