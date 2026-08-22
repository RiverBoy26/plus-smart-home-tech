package ru.practicum.service.handler.sensor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.mapper.EventMapper;
import ru.practicum.service.KafkaEventProducer;
import ru.yandex.practicum.grpc.telemetry.messages.sensor.SensorEventProto;

@Component
@RequiredArgsConstructor
public class TemperatureSensorEvent implements SensorEventHandler {
    private final KafkaEventProducer producer;
    private static final String SENSORS_TOPIC = "telemetry.sensors.v1";

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.TEMPERATURE_SENSOR;
    }

    @Override
    public void handle(SensorEventProto event) {
        producer.send(SENSORS_TOPIC, EventMapper.toAvroFromTemperatureSensorProto(event));
    }
}
