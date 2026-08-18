package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<Void> collectHubEvent(@RequestBody HubEvent event) {
        eventService.collectHubEvent(event);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/sensors")
    public ResponseEntity<Void> collectSensorEvent(@RequestBody SensorEvent event) {
        eventService.collectSensorEvent(event);
        return ResponseEntity.ok().build();
    }
}
