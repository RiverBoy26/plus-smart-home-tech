package ru.practicum.service.hub.handler;

import ru.yandex.practicum.kafka.telemetry.hub.HubEventAvro;

public interface HubEventHandler {
    Class<?> getEventType();

    void handle(HubEventAvro event);
}