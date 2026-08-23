package ru.practicum.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.config.HubKafkaConfig;
import ru.practicum.service.hub.HubEventService;
import ru.yandex.practicum.kafka.telemetry.hub.HubEventAvro;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class HubEventProcessor implements Runnable {
    private static final Duration CONSUME_ATTEMPT_TIMEOUT = Duration.ofMillis(1000);
    private static final List<String> HUB_EVENT_TOPIC = List.of("telemetry.hubs.v1");
    private static final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();
    private final KafkaConsumer<Void, HubEventAvro> consumer;
    private final HubEventService hubEventService;

    public HubEventProcessor(HubKafkaConfig config, HubEventService hubEventService) {
        this.consumer = new KafkaConsumer<>(config.getProperties());
        this.hubEventService = hubEventService;
    }

    @Override
    public void run() {
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

        try {
            consumer.subscribe(HUB_EVENT_TOPIC);
            while (true) {
                ConsumerRecords<Void, HubEventAvro> records = consumer.poll(CONSUME_ATTEMPT_TIMEOUT);

                int processedMessagesCount = 0;
                for (ConsumerRecord<Void, HubEventAvro> record : records) {
                    handleRecord(record);
                    processedMessagesCount++;
                    manageOffsets(record, processedMessagesCount, consumer);
                }
            }

        } catch (WakeupException ignored) {
        } catch (Exception e) {
            log.error("Error while processing events from hubs", e);
        } finally {

            try {
                consumer.commitSync(currentOffsets);

            } finally {
                log.info("Consumer shutting down");
                consumer.close();
            }
        }
    }

    private void manageOffsets(ConsumerRecord<Void, HubEventAvro> record,
                               int processedMessagesCount,
                               KafkaConsumer<Void, HubEventAvro> consumer) {
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

    private void handleRecord(ConsumerRecord<Void, HubEventAvro> record) {
        log.info("topic = {}, partition = {}, offset = {}, value: {}\n",
                record.topic(), record.partition(), record.offset(), record.value());

        hubEventService.handleHubEvent(record.value());
    }
}