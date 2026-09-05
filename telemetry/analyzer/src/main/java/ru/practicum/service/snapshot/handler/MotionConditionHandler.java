package ru.practicum.service.snapshot.handler;

import org.springframework.stereotype.Component;
import ru.practicum.model.Condition;
import ru.practicum.model.ScenarioCondition;
import ru.yandex.practicum.kafka.telemetry.sensor.MotionSensorAvro;
import ru.yandex.practicum.kafka.telemetry.snapshot.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.snapshot.SensorsSnapshotAvro;

@Component
public class MotionConditionHandler implements ConditionHandler {
    @Override
    public String getType() {
        return "MOTION";
    }

    @Override
    public boolean check(ScenarioCondition scenarioCondition, SensorsSnapshotAvro sensorsSnapshotAvro) {
        Condition condition = scenarioCondition.getCondition();
        String sensorId = scenarioCondition.getSensor().getId();
        SensorStateAvro state = sensorsSnapshotAvro.getSensorsState().get(sensorId);

        if (state == null) {
            return false;
        }

        MotionSensorAvro data = (MotionSensorAvro) state.getData();

        if (condition.getOperation().equals("EQUALS")) {
            return data.getMotion() != (condition.getValue() == 1);
        }
        return false;
    }
}