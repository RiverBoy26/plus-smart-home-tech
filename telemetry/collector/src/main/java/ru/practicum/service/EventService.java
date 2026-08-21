package ru.practicum.service;

import ru.practicum.model.hub.HubEvent;
import ru.practicum.model.sensor.SensorEvent;

public interface EventService {
    HubEvent collectHubEvent(HubEvent event);

    SensorEvent collectSensorEvent(SensorEvent event);
}