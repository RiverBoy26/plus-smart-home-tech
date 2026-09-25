package ru.practicum.service.hub.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.model.Sensor;
import ru.practicum.repository.SensorRepository;
import ru.yandex.practicum.kafka.telemetry.hub.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.hub.device.DeviceAddedEventAvro;

@Component
@RequiredArgsConstructor
public class DeviceAddedHandler implements HubEventHandler {
    private final SensorRepository sensorRepository;

    @Override
    public Class<?> getEventType() {
        return DeviceAddedEventAvro.class;
    }

    @Override
    public void handle(HubEventAvro event) {
        DeviceAddedEventAvro deviceAddedEvent = (DeviceAddedEventAvro) event.getPayload();
        Sensor sensor = new Sensor();

        sensor.setId(deviceAddedEvent.getId());
        sensor.setHubId(event.getHubId());

        sensorRepository.save(sensor);
    }
}