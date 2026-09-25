package ru.practicum.service.hub.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.repository.SensorRepository;
import ru.yandex.practicum.kafka.telemetry.hub.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.hub.device.DeviceRemovedEventAvro;

@Component
@RequiredArgsConstructor
public class DeviceRemovedHandler implements HubEventHandler {
    private final SensorRepository sensorRepository;

    @Override
    public Class<?> getEventType() {
        return DeviceRemovedEventAvro.class;
    }

    @Override
    public void handle(HubEventAvro event) {
        DeviceRemovedEventAvro deviceRemovedEvent = (DeviceRemovedEventAvro) event.getPayload();
        sensorRepository.deleteById(deviceRemovedEvent.getId());
    }
}