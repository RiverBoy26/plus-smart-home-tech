package ru.practicum.service.handler.hub.device;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.mapper.EventMapper;
import ru.practicum.service.KafkaEventProducer;
import ru.practicum.service.handler.hub.HubEventHandler;
import ru.yandex.practicum.grpc.telemetry.messages.hub.HubEventProto;

@Component
@RequiredArgsConstructor
public class DeviceAddedEventHandler implements HubEventHandler {
    private final KafkaEventProducer producer;
    private static final String HUBS_TOPIC = "telemetry.hubs.v1";

    @Override
    public void handle(HubEventProto event) {
        producer.send(HUBS_TOPIC, EventMapper.toAvroFromDeviceAddedProto(event));
    }

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.DEVICE_ADDED;
    }
}
