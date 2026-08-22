package ru.practicum.service.handler.hub.scenario;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.mapper.EventMapper;
import ru.practicum.service.KafkaEventProducer;
import ru.practicum.service.handler.hub.HubEventHandler;
import ru.yandex.practicum.grpc.telemetry.messages.hub.HubEventProto;

@Component
@RequiredArgsConstructor
public class ScenarioAddedEventHandler implements HubEventHandler {
    private final KafkaEventProducer producer;
    private static final String HUBS_TOPIC = "telemetry.hubs.v1";

    @Override
    public void handle(HubEventProto event) {
        producer.send(HUBS_TOPIC, EventMapper.toAvroFromScenarioAddedProto(event));
    }

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.SCENARIO_ADDED;
    }
}
