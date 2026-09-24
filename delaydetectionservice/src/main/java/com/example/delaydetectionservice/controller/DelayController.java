package com.example.delaydetectionservice.controller;

import com.example.delaydetectionservice.service.DelayService;
import org.example.common.dto.FlightStatusUpdateDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/delay")
public class DelayController {

    private final DelayService delayService;

    public DelayController(DelayService delayService) {
        this.delayService = delayService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(@RequestBody FlightStatusUpdateDTO update) {
        delayService.handleRawUpdate(update);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/simulate")
    public ResponseEntity<Void> simulate(@RequestBody FlightStatusUpdateDTO update) {
        delayService.handleRawUpdate(update);
        return ResponseEntity.ok().build();
    }
}
