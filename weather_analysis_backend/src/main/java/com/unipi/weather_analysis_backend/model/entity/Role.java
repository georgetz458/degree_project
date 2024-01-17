package com.unipi.weather_analysis_backend.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import java.util.HashSet;
import java.util.Set;

@Entity
public class Role {
    @Id
    private String role;

    @OneToMany(mappedBy = "role")
    private Set<User> users = new HashSet<>();


    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

}
