package ru.practicum.service.snapshot.handler;

import org.springframework.stereotype.Component;
import ru.practicum.model.Condition;
import ru.practicum.model.ScenarioCondition;
import ru.yandex.practicum.kafka.telemetry.sensor.LightSensorAvro;
import ru.yandex.practicum.kafka.telemetry.snapshot.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.snapshot.SensorsSnapshotAvro;

@Component
public class LuminosityConditionHandler implements ConditionHandler {
    @Override
    public String getType() {
        return "LUMINOSITY";
    }

    @Override
    public boolean check(ScenarioCondition scenarioCondition, SensorsSnapshotAvro sensorsSnapshotAvro) {
        Condition condition = scenarioCondition.getCondition();
        String sensorId = scenarioCondition.getSensor().getId();
        SensorStateAvro state = sensorsSnapshotAvro.getSensorsState().get(sensorId);

        if (state == null) {
            return false;
        }

        LightSensorAvro data = (LightSensorAvro) state.getData();

        if (condition.getOperation().equals("LOWER_THAN")) {
            return data.getLuminosity() < condition.getValue();
        }
        return false;
    }
}