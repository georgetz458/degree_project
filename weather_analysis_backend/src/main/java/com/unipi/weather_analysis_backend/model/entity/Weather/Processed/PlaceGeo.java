package com.unipi.weather_analysis_backend.model.entity.Weather.Processed;

import com.unipi.weather_analysis_backend.model.entity.Municipality;
import com.unipi.weather_analysis_backend.model.entity.Weather.IdClass.PlaceId;
import jakarta.persistence.*;
import org.locationtech.jts.geom.Point;


@Entity
@IdClass(PlaceId.class)
public class PlaceGeo {
    @Id
    private double lon;
    @Id
    private double lat;

    @Column(columnDefinition = "geometry")
    private Point point;

    @ManyToOne
    @JoinColumn(name = "municipality", referencedColumnName = "name", nullable = false)
    private Municipality municipality;





    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public Point getPoint() {
        return point;
    }


    public Municipality getMunicipality() {
        return municipality;
    }


}
