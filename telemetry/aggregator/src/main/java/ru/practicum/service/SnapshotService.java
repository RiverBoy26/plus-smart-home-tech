package ru.practicum.service;

import ru.yandex.practicum.kafka.telemetry.sensor.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.snapshot.SensorsSnapshotAvro;

import java.util.Optional;

public interface SnapshotService {
    Optional<SensorsSnapshotAvro> updateSnapshot(SensorEventAvro sensorEvent);
}