package ru.practicum.processor;

import org.springframework.stereotype.Component;
import ru.practicum.config.HubKafkaConfig;
import ru.practicum.config.KafkaConfig;
import ru.practicum.service.hub.HubEventService;
import ru.yandex.practicum.kafka.telemetry.hub.HubEventAvro;

@Component
public class HubEventProcessor extends AbstractKafkaProcessor<HubEventAvro> {
    private final HubEventService hubEventService;

    public HubEventProcessor(HubKafkaConfig config,
                             KafkaConfig commonConfig,
                             HubEventService hubEventService) {
        super(
                config.getProperties(),
                config.getTopic(),
                commonConfig.getPollTimeout(),
                commonConfig.getCommitBatchSize()
        );
        this.hubEventService = hubEventService;
    }

    @Override
    protected void handleEvent(HubEventAvro event) {
        hubEventService.handleHubEvent(event);
    }
}