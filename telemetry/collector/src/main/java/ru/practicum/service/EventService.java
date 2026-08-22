package ru.practicum.service;

import ru.practicum.model.hub.HubEvent;
import ru.practicum.model.sensor.SensorEvent;
import ru.yandex.practicum.grpc.telemetry.messages.hub.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.messages.sensor.SensorEventProto;

public interface EventService {
    void collectHubEvent(HubEventProto event);

    void collectSensorEvent(SensorEventProto event);
}