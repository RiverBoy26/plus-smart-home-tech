package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.hub.HubEvent;
import ru.practicum.model.sensor.SensorEvent;
import ru.yandex.practicum.kafka.telemetry.hub.HubEventAvro;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final KafkaEventProducer producer;
    private static final String HUBS_TOPIC = "telemetry.hubs.v1";
    private static final String SENSORS_TOPIC = "telemetry.sensors.v1";

    @Override
    public HubEvent collectHubEvent(HubEvent event) {
        log.info("Событие хаба: {}", event);
        HubEventAvro hubEventAvro = EventMapper.toHubEventAvro(event);
        producer.send(HUBS_TOPIC, hubEventAvro);
        return event;
    }

    @Override
    public SensorEvent collectSensorEvent(SensorEvent event) {
        log.info("Событие датчика: {}", event);
        producer.send(SENSORS_TOPIC, EventMapper.toSensorEventAvro(event));
        return event;
    }
}