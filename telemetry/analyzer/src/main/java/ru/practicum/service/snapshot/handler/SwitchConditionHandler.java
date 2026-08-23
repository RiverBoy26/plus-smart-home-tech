package ru.practicum.service.snapshot.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.model.Condition;
import ru.practicum.model.ScenarioCondition;
import ru.yandex.practicum.kafka.telemetry.sensor.SwitchSensorAvro;
import ru.yandex.practicum.kafka.telemetry.snapshot.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.snapshot.SensorsSnapshotAvro;

@Component
@Slf4j
public class SwitchConditionHandler implements ConditionHandler {
    @Override
    public String getType() {
        return "SWITCH";
    }

    @Override
    public boolean check(ScenarioCondition scenarioCondition, SensorsSnapshotAvro sensorsSnapshotAvro) {
        Condition condition = scenarioCondition.getCondition();
        String sensorId = scenarioCondition.getSensor().getId();
        SensorStateAvro state = sensorsSnapshotAvro.getSensorsState().get(sensorId);

        if (state == null) {
            return false;
        }

        SwitchSensorAvro data = (SwitchSensorAvro) state.getData();

        log.info(
                "SWITCH condition: sensorId={}, actualState={}, operation={}, expectedValue={}, result={}",
                sensorId,
                data.getState(),
                condition.getOperation(),
                condition.getValue(),
                data.getState() == (condition.getValue() == 1)
        );

        if (condition.getOperation().equals("EQUALS")) {
            return data.getState() == ((condition.getValue()) == 1);
        }
        return false;
    }
}