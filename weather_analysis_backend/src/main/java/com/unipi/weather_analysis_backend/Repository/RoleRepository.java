package com.unipi.weather_analysis_backend.Repository;

import com.unipi.weather_analysis_backend.model.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, String> {
    public Role findByRole( String role);
}
