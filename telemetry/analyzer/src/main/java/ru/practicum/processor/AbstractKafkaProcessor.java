package ru.practicum.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Slf4j
public abstract class AbstractKafkaProcessor<T> implements Runnable {
    private final Duration consumeAttemptTimeout;
    private final int commitBatchSize;
    private final String topic;
    private final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();
    private final KafkaConsumer<Void, T> consumer;

    protected AbstractKafkaProcessor(Properties properties,
                                     String topic,
                                     Duration consumeAttemptTimeout,
                                     int commitBatchSize) {
        this.consumer = new KafkaConsumer<>(properties);
        this.topic = topic;
        this.consumeAttemptTimeout = consumeAttemptTimeout;
        this.commitBatchSize = commitBatchSize;
    }

    @Override
    public void run() {
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

        try {
            consumer.subscribe(List.of(topic));
            while (true) {
                ConsumerRecords<Void, T> records = consumer.poll(consumeAttemptTimeout);

                int processedMessagesCount = 0;
                for (ConsumerRecord<Void, T> record : records) {
                    handleRecord(record);
                    processedMessagesCount++;
                    manageOffsets(record, processedMessagesCount);
                }
            }
        } catch (WakeupException ignored) {
        } catch (Exception e) {
            log.error("Error while processing events from topic {}", topic, e);
        } finally {
            try {
                consumer.commitSync(currentOffsets);
            } finally {
                log.info("Consumer for topic {} shutting down", topic);
                consumer.close();
            }
        }
    }

    public void start() {
        run();
    }

    protected abstract void handleEvent(T event);

    private void handleRecord(ConsumerRecord<Void, T> record) {
        log.info("topic = {}, partition = {}, offset = {}, value: {}\n",
                record.topic(), record.partition(), record.offset(), record.value());

        handleEvent(record.value());
    }

    private void manageOffsets(ConsumerRecord<Void, T> record, int processedMessagesCount) {
        currentOffsets.put(
                new TopicPartition(record.topic(), record.partition()),
                new OffsetAndMetadata(record.offset() + 1)
        );

        if (processedMessagesCount % commitBatchSize == 0) {
            consumer.commitAsync(currentOffsets, (offsets, exception) -> {
                if (exception != null) {
                    log.warn("Error while committing offsets: {}", offsets, exception);
                }
            });
        }
    }
}