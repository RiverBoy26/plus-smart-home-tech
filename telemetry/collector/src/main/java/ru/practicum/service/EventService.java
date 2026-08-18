package ru.practicum.service;

import ru.practicum.model.hub.HubEvent;
import ru.practicum.model.sensor.SensorEvent;

public interface EventService {
    void collectHubEvent(HubEvent event);

    void collectSensorEvent(SensorEvent event);
}