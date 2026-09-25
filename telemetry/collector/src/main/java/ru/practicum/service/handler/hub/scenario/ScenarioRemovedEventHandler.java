package ru.practicum.service.handler.hub.scenario;

import org.springframework.stereotype.Component;
import ru.practicum.mapper.EventMapper;
import ru.practicum.service.KafkaEventProducer;
import ru.yandex.practicum.grpc.telemetry.messages.hub.HubEventProto;

@Component
public class ScenarioRemovedEventHandler extends ScenarioEventHandler {

    public ScenarioRemovedEventHandler(KafkaEventProducer producer) {
        super(producer);
    }

    @Override
    public void handle(HubEventProto event) {
        send(EventMapper.toAvroFromScenarioRemovedProto(event));
    }

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.SCENARIO_REMOVED;
    }
}