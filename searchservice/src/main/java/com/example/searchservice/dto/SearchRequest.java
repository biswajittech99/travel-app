package com.example.searchservice.dto;

import org.springframework.lang.Contract;

import java.time.LocalDate;

public record SearchRequest(String from, String to, LocalDate date) {


}
