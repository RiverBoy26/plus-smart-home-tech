package ru.practicum.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.config.KafkaConsumerConfig;
import ru.practicum.config.KafkaProducerConfig;
import ru.yandex.practicum.kafka.telemetry.sensor.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.snapshot.SensorsSnapshotAvro;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
public class AggregationStarter {

    private static final Duration CONSUME_ATTEMPT_TIMEOUT = Duration.ofMillis(1000);
    private static final List<String> SENSORS_TOPIC = List.of("telemetry.sensors.v1");
    private static final String SNAPSHOTS_TOPIC = "telemetry.snapshots.v1";

    private static final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();

    private final KafkaProducer<String, SpecificRecordBase> producer;
    private final KafkaConsumer<Void, SensorEventAvro> consumer;
    private final SnapshotService snapshotService;

    private final AtomicBoolean shutdownRequested = new AtomicBoolean(false);

    public AggregationStarter(KafkaConsumerConfig kafkaConsumerConfig,
                              KafkaProducerConfig kafkaProducerConfig) {
        this.producer = new KafkaProducer<>(kafkaProducerConfig.getProperties());
        this.consumer = new KafkaConsumer<>(kafkaConsumerConfig.getProperties());
        this.snapshotService = new SnapshotServiceImpl();
    }

    public void start() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Получен сигнал завершения работы. Выполняется остановка Kafka Consumer");
            shutdownRequested.set(true);
            consumer.wakeup();
        }));

        try {
            consumer.subscribe(SENSORS_TOPIC);

            while (!shutdownRequested.get()) {
                ConsumerRecords<Void, SensorEventAvro> records = consumer.poll(CONSUME_ATTEMPT_TIMEOUT);

                int processedMessagesCount = 0;

                for (ConsumerRecord<Void, SensorEventAvro> record : records) {
                    handleRecord(record);
                    processedMessagesCount++;
                    manageOffsets(record, processedMessagesCount, consumer);
                }
            }

        } catch (WakeupException e) {
            if (!shutdownRequested.get()) {
                log.error("Неожиданное прерывание работы Kafka Consumer", e);
                throw e;
            }

            log.info("Kafka Consumer успешно прерван для штатного завершения работы");

        } catch (Exception e) {
            log.error("Ошибка при обработке событий от датчиков", e);

        } finally {
            try {
                consumer.commitSync(currentOffsets);
                producer.flush();
            } finally {
                log.info("Закрытие Kafka Consumer");
                consumer.close();

                log.info("Закрытие Kafka Producer");
                producer.close();
            }
        }
    }

    private void manageOffsets(ConsumerRecord<Void, SensorEventAvro> record,
                               int processedMessagesCount,
                               KafkaConsumer<Void, SensorEventAvro> consumer) {
        currentOffsets.put(
                new TopicPartition(record.topic(), record.partition()),
                new OffsetAndMetadata(record.offset() + 1)
        );

        if (processedMessagesCount % 10 == 0) {
            consumer.commitAsync(currentOffsets, (offsets, exception) -> {
                if (exception != null) {
                    log.warn("Ошибка при сохранении оффсетов: {}", offsets, exception);
                }
            });
        }
    }

    private void handleRecord(ConsumerRecord<Void, SensorEventAvro> record) {
        log.info(
                "Получено сообщение: топик = {}, партиция = {}, оффсет = {}, значение = {}",
                record.topic(),
                record.partition(),
                record.offset(),
                record.value()
        );

        Optional<SensorsSnapshotAvro> snapshot = snapshotService.updateSnapshot(record.value());

        snapshot.ifPresent(sensorsSnapshotAvro -> send(SNAPSHOTS_TOPIC, sensorsSnapshotAvro));
    }

    private void send(String topic, SpecificRecordBase value) {
        ProducerRecord<String, SpecificRecordBase> record = new ProducerRecord<>(topic, value);
        producer.send(record);
    }
}