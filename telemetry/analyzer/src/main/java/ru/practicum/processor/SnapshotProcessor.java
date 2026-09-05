package ru.practicum.processor;

import org.springframework.stereotype.Component;
import ru.practicum.config.KafkaConfig;
import ru.practicum.config.SnapshotKafkaConfig;
import ru.practicum.service.snapshot.SnapshotService;
import ru.yandex.practicum.kafka.telemetry.snapshot.SensorsSnapshotAvro;

@Component
public class SnapshotProcessor extends AbstractKafkaProcessor<SensorsSnapshotAvro> {
    private final SnapshotService snapshotService;

    public SnapshotProcessor(SnapshotKafkaConfig config,
                             KafkaConfig commonConfig,
                             SnapshotService snapshotService) {
        super(
                config.getProperties(),
                config.getTopic(),
                commonConfig.getPollTimeout(),
                commonConfig.getCommitBatchSize()
        );
        this.snapshotService = snapshotService;
    }

    @Override
    protected void handleEvent(SensorsSnapshotAvro event) {
        snapshotService.handleSnapshot(event);
    }
}