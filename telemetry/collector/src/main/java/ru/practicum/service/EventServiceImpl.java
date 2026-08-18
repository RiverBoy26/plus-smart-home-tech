package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.hub.HubEvent;
import ru.practicum.model.sensor.SensorEvent;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final KafkaEventProducer producer;
    private static final String HUBS_TOPIC = "telemetry.hubs.v1";
    private static final String SENSORS_TOPIC = "telemetry.sensors.v1";

    @Override
    public void collectHubEvent(HubEvent event) {
        log.info("Событие хаба: {}", event);
        producer.send(HUBS_TOPIC, EventMapper.toHubEventAvro(event));
    }

    @Override
    public void collectSensorEvent(SensorEvent event) {
        log.info("Событие датчика: {}", event);
        producer.send(SENSORS_TOPIC, EventMapper.toSensorEventAvro(event));
    }
}