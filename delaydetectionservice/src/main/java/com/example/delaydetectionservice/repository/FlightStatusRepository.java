package com.example.delaydetectionservice.repository;

import com.example.delaydetectionservice.entity.FlightStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlightStatusRepository extends JpaRepository<FlightStatus, String> {
}
