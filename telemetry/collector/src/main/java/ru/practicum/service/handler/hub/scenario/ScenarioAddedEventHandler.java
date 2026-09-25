package ru.practicum.service.handler.hub.scenario;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.mapper.EventMapper;
import ru.practicum.service.KafkaEventProducer;
import ru.practicum.service.handler.hub.HubEventHandler;
import ru.yandex.practicum.grpc.telemetry.messages.hub.HubEventProto;

@Component
public class ScenarioAddedEventHandler extends ScenarioEventHandler {

    public ScenarioAddedEventHandler(KafkaEventProducer producer) {
        super(producer);
    }

    @Override
    public void handle(HubEventProto event) {
        send(EventMapper.toAvroFromScenarioAddedProto(event));
    }

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.SCENARIO_ADDED;
    }
}
