package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.model.hub.HubEvent;
import ru.practicum.model.sensor.SensorEvent;
import ru.practicum.service.EventService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class EventController {
    public final EventService eventService;

    @PostMapping("/hubs")
    public ResponseEntity<HubEvent> collectHubEvent(@RequestBody HubEvent event) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(eventService.collectHubEvent(event));
    }

    @PostMapping("/sensors")
    public ResponseEntity<SensorEvent> collectSensorEvent(@RequestBody SensorEvent event) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(eventService.collectSensorEvent(event));
    }
}
