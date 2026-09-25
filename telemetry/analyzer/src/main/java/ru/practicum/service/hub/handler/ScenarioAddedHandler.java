package ru.practicum.service.hub.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.model.*;
import ru.practicum.repository.*;
import ru.yandex.practicum.kafka.telemetry.hub.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.hub.scenario.DeviceActionAvro;
import ru.yandex.practicum.kafka.telemetry.hub.scenario.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.hub.scenario.ScenarioConditionAvro;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class ScenarioAddedHandler implements HubEventHandler {

    private final ScenarioRepository scenarioRepository;
    private final SensorRepository sensorRepository;
    private final ConditionRepository conditionRepository;
    private final ScenarioConditionRepository scenarioConditionRepository;
    private final ActionRepository actionRepository;
    private final ScenarioActionRepository scenarioActionRepository;

    @Override
    public Class<?> getEventType() {
        return ScenarioAddedEventAvro.class;
    }

    @Override
    public void handle(HubEventAvro event) {
        ScenarioAddedEventAvro scenarioAddedEvent =
                (ScenarioAddedEventAvro) event.getPayload();

        Scenario scenario = new Scenario();
        scenario.setHubId(event.getHubId());
        scenario.setName(scenarioAddedEvent.getName());

        scenarioRepository.save(scenario);

        Map<String, Sensor> sensorsById =
                getSensorsById(scenarioAddedEvent);

        saveConditions(
                scenario,
                scenarioAddedEvent.getConditions(),
                sensorsById
        );

        saveActions(
                scenario,
                scenarioAddedEvent.getActions(),
                sensorsById
        );
    }

    private Map<String, Sensor> getSensorsById(
            ScenarioAddedEventAvro event
    ) {
        Set<String> sensorIds = Stream.concat(
                event.getConditions().stream()
                        .map(ScenarioConditionAvro::getSensorId),
                event.getActions().stream()
                        .map(DeviceActionAvro::getSensorId)
        ).collect(Collectors.toSet());

        return sensorRepository.findByIdIn(sensorIds)
                .stream()
                .collect(Collectors.toMap(
                        Sensor::getId,
                        Function.identity()
                ));
    }

    private void saveConditions(
            Scenario scenario,
            List<ScenarioConditionAvro> conditionsAvro,
            Map<String, Sensor> sensorsById
    ) {
        List<Condition> conditions = new ArrayList<>();

        for (ScenarioConditionAvro conditionAvro : conditionsAvro) {
            validateSensor(
                    conditionAvro.getSensorId(),
                    sensorsById
            );

            Condition condition = new Condition();

            condition.setType(
                    conditionAvro.getType().name()
            );

            condition.setOperation(
                    conditionAvro.getOperation().name()
            );

            switch (conditionAvro.getValue()) {
                case null ->
                        condition.setValue(null);

                case Integer value ->
                        condition.setValue(value);

                case Boolean value ->
                        condition.setValue(value ? 1 : 0);

                default -> throw new IllegalArgumentException(
                        "Unsupported condition value type: "
                                + conditionAvro.getValue()
                                .getClass()
                );
            }

            conditions.add(condition);
        }

        conditionRepository.saveAll(conditions);

        List<ScenarioCondition> scenarioConditions =
                new ArrayList<>();

        for (int i = 0; i < conditionsAvro.size(); i++) {
            ScenarioConditionAvro conditionAvro =
                    conditionsAvro.get(i);

            Condition condition = conditions.get(i);

            Sensor sensor = sensorsById.get(
                    conditionAvro.getSensorId()
            );

            ScenarioCondition scenarioCondition =
                    new ScenarioCondition();

            scenarioCondition.setId(
                    new ScenarioConditionId(
                            scenario.getId(),
                            sensor.getId(),
                            condition.getId()
                    )
            );

            scenarioCondition.setScenario(scenario);
            scenarioCondition.setSensor(sensor);
            scenarioCondition.setCondition(condition);

            scenarioConditions.add(scenarioCondition);
        }

        scenarioConditionRepository.saveAll(
                scenarioConditions
        );
    }

    private void saveActions(
            Scenario scenario,
            List<DeviceActionAvro> actionsAvro,
            Map<String, Sensor> sensorsById
    ) {
        List<Action> actions = new ArrayList<>();

        for (DeviceActionAvro actionAvro : actionsAvro) {
            validateSensor(
                    actionAvro.getSensorId(),
                    sensorsById
            );

            Action action = new Action();

            action.setType(
                    actionAvro.getType().name()
            );

            action.setValue(
                    actionAvro.getValue()
            );

            actions.add(action);
        }

        actionRepository.saveAll(actions);

        List<ScenarioAction> scenarioActions =
                new ArrayList<>();

        for (int i = 0; i < actionsAvro.size(); i++) {
            DeviceActionAvro actionAvro =
                    actionsAvro.get(i);

            Action action = actions.get(i);

            Sensor sensor = sensorsById.get(
                    actionAvro.getSensorId()
            );

            ScenarioAction scenarioAction =
                    new ScenarioAction();

            scenarioAction.setId(
                    new ScenarioActionId(
                            scenario.getId(),
                            sensor.getId(),
                            action.getId()
                    )
            );

            scenarioAction.setScenario(scenario);
            scenarioAction.setSensor(sensor);
            scenarioAction.setAction(action);

            scenarioActions.add(scenarioAction);
        }

        scenarioActionRepository.saveAll(
                scenarioActions
        );
    }

    private void validateSensor(
            String sensorId,
            Map<String, Sensor> sensorsById
    ) {
        if (!sensorsById.containsKey(sensorId)) {
            throw new IllegalArgumentException(
                    "Sensor not found: " + sensorId
            );
        }
    }
}