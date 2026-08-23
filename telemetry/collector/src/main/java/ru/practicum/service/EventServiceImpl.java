package ru.practicum.service;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.service.handler.hub.HubEventHandler;
import ru.practicum.service.handler.sensor.SensorEventHandler;
import ru.yandex.practicum.grpc.telemetry.messages.hub.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.messages.sensor.SensorEventProto;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EventServiceImpl implements EventService {
    private final Map<SensorEventProto.PayloadCase, SensorEventHandler> sensorEventHandlers;
    private final Map<HubEventProto.PayloadCase, HubEventHandler> hubEventHandlers;

    public EventServiceImpl(Set<SensorEventHandler> sensorEventHandlers, Set<HubEventHandler> hubEventHandlers) {
        this.sensorEventHandlers = sensorEventHandlers.stream()
                .collect(Collectors.toMap(
                        SensorEventHandler::getMessageType,
                        Function.identity()
                ));
        this.hubEventHandlers = hubEventHandlers.stream()
                .collect(Collectors.toMap(
                        HubEventHandler::getMessageType,
                        Function.identity()
                ));
    }

    @Override
    public void collectHubEvent(HubEventProto event) {
        try {
            log.info("json: {}", JsonFormat.printer().print(event));
        } catch (InvalidProtocolBufferException e) {
            log.warn("Error occurred while processing HubEvent", e);
        }

        if (hubEventHandlers.containsKey(event.getPayloadCase())) {
            hubEventHandlers.get(event.getPayloadCase()).handle(event);
        } else {
            throw new IllegalArgumentException("HubEventHandler not found for type: "
                    + event.getPayloadCase());
        }
    }

    @Override
    public void collectSensorEvent(SensorEventProto event) {
        try {
            log.info("json: {}", JsonFormat.printer().print(event));
        } catch (InvalidProtocolBufferException e) {
            log.warn("Error occurred while processing SensorEvent", e);
        }

        if (sensorEventHandlers.containsKey(event.getPayloadCase())) {
            sensorEventHandlers.get(event.getPayloadCase()).handle(event);
        } else {
            throw new IllegalArgumentException("SensorEventHandler not found for type: "
                    + event.getPayloadCase());
        }
    }
}