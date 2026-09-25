package ru.practicum.service.hub;

import ru.yandex.practicum.kafka.telemetry.hub.HubEventAvro;

public interface HubEventService {
    void handleHubEvent(HubEventAvro event);
}