package ru.practicum.service.handler.hub.scenario;

import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecordBase;
import ru.practicum.service.KafkaEventProducer;
import ru.practicum.service.handler.hub.HubEventHandler;

@RequiredArgsConstructor
public abstract class ScenarioEventHandler implements HubEventHandler {
    private final KafkaEventProducer producer;
    private static final String HUBS_TOPIC = "telemetry.hubs.v1";

    protected void send(SpecificRecordBase event) {
        producer.send(HUBS_TOPIC, event);
    }
}
