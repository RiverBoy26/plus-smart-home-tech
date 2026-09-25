package ru.practicum.service.handler.sensor;

import ru.yandex.practicum.grpc.telemetry.messages.sensor.SensorEventProto;

public interface SensorEventHandler {
    SensorEventProto.PayloadCase getMessageType();

    void handle(SensorEventProto event);
}
