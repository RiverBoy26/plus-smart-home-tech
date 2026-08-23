package ru.practicum.service.snapshot;

import ru.yandex.practicum.kafka.telemetry.snapshot.SensorsSnapshotAvro;

public interface SnapshotService {
    void handleSnapshot(SensorsSnapshotAvro sensorsSnapshotAvro);
}