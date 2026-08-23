package ru.practicum.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.config.SnapshotKafkaConfig;
import ru.practicum.service.snapshot.SnapshotService;
import ru.yandex.practicum.kafka.telemetry.snapshot.SensorsSnapshotAvro;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class SnapshotProcessor {
    private static final Duration CONSUME_ATTEMPT_TIMEOUT = Duration.ofMillis(1000);
    private static final List<String> SNAPSHOTS_TOPIC = List.of("telemetry.snapshots.v1");
    private static final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();
    private final KafkaConsumer<Void, SensorsSnapshotAvro> consumer;
    private final SnapshotService snapshotService;

    public SnapshotProcessor(SnapshotKafkaConfig config, SnapshotService snapshotService) {
        this.consumer = new KafkaConsumer<>(config.getProperties());
        this.snapshotService = snapshotService;
    }

    public void start() {
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

        try {
            consumer.subscribe(SNAPSHOTS_TOPIC);
            while (true) {
                ConsumerRecords<Void, SensorsSnapshotAvro> records = consumer.poll(CONSUME_ATTEMPT_TIMEOUT);

                int processedMessagesCount = 0;
                for (ConsumerRecord<Void, SensorsSnapshotAvro> record : records) {
                    handleRecord(record);
                    processedMessagesCount++;
                    manageOffsets(record, processedMessagesCount, consumer);
                }
            }

        } catch (WakeupException ignored) {
        } catch (Exception e) {
            log.error("Error while processing events from sensors", e);
        } finally {

            try {
                consumer.commitSync(currentOffsets);

            } finally {
                log.info("Consumer shutting down");
                consumer.close();
            }
        }
    }

    private void manageOffsets(ConsumerRecord<Void, SensorsSnapshotAvro> record,
                               int processedMessagesCount,
                               KafkaConsumer<Void, SensorsSnapshotAvro> consumer) {
        currentOffsets.put(
                new TopicPartition(record.topic(), record.partition()),
                new OffsetAndMetadata(record.offset() + 1)
        );

        if (processedMessagesCount % 10 == 0) {
            consumer.commitAsync(currentOffsets, (offsets, exception) -> {
                if (exception != null) {
                    log.warn("Error while committing offsets: {}", offsets, exception);
                }
            });
        }
    }

    private void handleRecord(ConsumerRecord<Void, SensorsSnapshotAvro> record) {
        log.info("topic = {}, partition = {}, offset = {}, value: {}\n",
                record.topic(), record.partition(), record.offset(), record.value());

        snapshotService.handleSnapshot(record.value());
    }
}