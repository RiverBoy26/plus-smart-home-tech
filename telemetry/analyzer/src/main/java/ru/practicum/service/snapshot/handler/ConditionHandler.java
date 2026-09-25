package ru.practicum.service.snapshot.handler;

import ru.practicum.model.ScenarioCondition;
import ru.yandex.practicum.kafka.telemetry.snapshot.SensorsSnapshotAvro;

public interface ConditionHandler {
    String getType();

    boolean check(ScenarioCondition condition, SensorsSnapshotAvro sensor);
}