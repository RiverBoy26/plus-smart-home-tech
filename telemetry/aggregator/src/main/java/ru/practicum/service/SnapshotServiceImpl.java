package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.sensor.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.snapshot.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.snapshot.SensorsSnapshotAvro;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SnapshotServiceImpl implements SnapshotService {
    private final Map<String, SensorsSnapshotAvro> sensorSnapshots = new HashMap<>();

    public Optional<SensorsSnapshotAvro> updateSnapshot(SensorEventAvro sensorEvent) {
        sensorSnapshots.computeIfAbsent(sensorEvent.getHubId(),
                hubId -> new SensorsSnapshotAvro(hubId, sensorEvent.getTimestamp(), new HashMap<>()));

        SensorStateAvro oldState = sensorSnapshots.get(sensorEvent.getHubId()).getSensorsState()
                .get(sensorEvent.getId());

        if (oldState != null) {
            if (oldState.getTimestamp().isAfter(sensorEvent.getTimestamp()) ||
                    oldState.getData().equals(sensorEvent.getPayload())) {
                return Optional.empty();
            }
        }

        SensorStateAvro newState = new SensorStateAvro(
                sensorEvent.getTimestamp(),
                sensorEvent.getPayload()
        );
        sensorSnapshots.get(sensorEvent.getHubId()).getSensorsState().put(sensorEvent.getId(), newState);
        sensorSnapshots.get(sensorEvent.getHubId()).setTimestamp(sensorEvent.getTimestamp());

        return Optional.of(sensorSnapshots.get(sensorEvent.getHubId()));
    }
}