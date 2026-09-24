package com.example.chatbotservice.airport.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "airports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Airport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "iata_code", nullable = false, unique = true)
    private String iataCode;

    @Column(name = "icao_code")
    private String icaoCode;

    @Column(name = "airport_name")
    private String airportName;

    private String city;

    private String country;

    private Double latitude;

    private Double longitude;

    private String timezone;
}
