package com.unipi.weather_analysis_backend.model.entity;

import com.unipi.weather_analysis_backend.model.entity.Weather.Processed.PlaceGeo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import org.locationtech.jts.geom.MultiPolygon;

import java.util.HashSet;
import java.util.Set;

@Entity
public class Municipality {
    @Id
    private String name;

    @Column(columnDefinition = "geometry")
    private MultiPolygon theGeom;

    @OneToMany(mappedBy = "municipality")
    private Set<PlaceGeo> placeGeoSet = new HashSet<>();
}
