package com.unipi.weather_analysis_backend.Repository;

import com.unipi.weather_analysis_backend.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUserId(String userId);
}
