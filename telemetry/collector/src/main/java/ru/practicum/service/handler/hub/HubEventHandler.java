package ru.practicum.service.handler.hub;

import ru.yandex.practicum.grpc.telemetry.messages.hub.HubEventProto;

public interface HubEventHandler {
    HubEventProto.PayloadCase getMessageType();

    void handle(HubEventProto event);
}
