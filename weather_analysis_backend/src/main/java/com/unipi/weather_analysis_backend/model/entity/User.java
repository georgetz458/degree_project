package com.unipi.weather_analysis_backend.model.entity;


import jakarta.persistence.*;



//users stores all the users, including common users or simply users or admins
@Entity
@Table(name = "users")
public class User {
    @Id
    private String userId;




    @ManyToOne
    @JoinColumn(name = "role", referencedColumnName = "role", nullable = false)
    private Role role;
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public User() {
    }


}
