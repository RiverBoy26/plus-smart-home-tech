package ru.practicum.service;

import kafka.deserializer.SensorEventDeserializer;
import kafka.serializer.GeneralAvroSerializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.VoidDeserializer;
import org.apache.kafka.common.serialization.VoidSerializer;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.sensor.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.snapshot.SensorsSnapshotAvro;

import java.time.Duration;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationStarter {
    private static final Duration CONSUME_ATTEMPT_TIMEOUT = Duration.ofMillis(1000);
    private static final List<String> SENSORS_TOPIC = List.of("telemetry.sensors.v1");
    private static final String SNAPSHOTS_TOPIC = "telemetry.snapshots.v1";

    private static final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();

    private final KafkaProducer<String, SpecificRecordBase> producer;
    private final KafkaConsumer<Void, SensorEventAvro> consumer;
    private final SnapshotService snapshotService;

    public AggregationStarter() {
        this.producer = new KafkaProducer<>(getProducerProperties());
        this.consumer = new KafkaConsumer<>(getConsumerProperties());
        this.snapshotService = new SnapshotServiceImpl();
    }

    public void start() {
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

        try {
            consumer.subscribe(SENSORS_TOPIC);
            while (true) {
                ConsumerRecords<Void, SensorEventAvro> records = consumer.poll(CONSUME_ATTEMPT_TIMEOUT);

                int processedMessagesCount = 0;
                for (ConsumerRecord<Void, SensorEventAvro> record : records) {
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
                producer.flush();

            } finally {
                log.info("Consumer shutting down");
                consumer.close();
                log.info("Producer shutting down");
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
                    log.warn("Error while committing offsets: {}", offsets, exception);
                }
            });
        }
    }

    private void handleRecord(ConsumerRecord<Void, SensorEventAvro> record) {
        log.info("topic = {}, partition = {}, offset = {}, value: {}\n",
                record.topic(), record.partition(), record.offset(), record.value());

        Optional<SensorsSnapshotAvro> snapshot = snapshotService.updateSnapshot(record.value());

        snapshot.ifPresent(sensorsSnapshotAvro -> send(SNAPSHOTS_TOPIC, sensorsSnapshotAvro));
    }

    private Properties getConsumerProperties() {
        Properties properties = new Properties();

        properties.put(ConsumerConfig.CLIENT_ID_CONFIG, "SomeConsumer");
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "some.group.id");
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, VoidDeserializer.class.getCanonicalName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                SensorEventDeserializer.class.getCanonicalName());

        properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);
        properties.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, 3072000);
        properties.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, 307200);
        return properties;
    }

    private Properties getProducerProperties() {
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, VoidSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, GeneralAvroSerializer.class);
        return properties;
    }

    private void send(String topic, SpecificRecordBase value) {
        ProducerRecord<String, SpecificRecordBase> record = new ProducerRecord<>(topic, value);

        producer.send(record);
    }
}