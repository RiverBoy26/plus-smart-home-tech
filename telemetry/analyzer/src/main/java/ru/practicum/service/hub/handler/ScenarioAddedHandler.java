package ru.practicum.service.hub.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.model.*;
import ru.practicum.repository.*;
import ru.yandex.practicum.kafka.telemetry.hub.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.hub.scenario.DeviceActionAvro;
import ru.yandex.practicum.kafka.telemetry.hub.scenario.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.hub.scenario.ScenarioConditionAvro;

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
        ScenarioAddedEventAvro scenarioAddedEvent = (ScenarioAddedEventAvro) event.getPayload();

        Scenario scenario = new Scenario();
        scenario.setHubId(event.getHubId());
        scenario.setName(scenarioAddedEvent.getName());
        scenarioRepository.save(scenario);

        Set<String> sensorIds = Stream.concat(
                scenarioAddedEvent.getConditions().stream()
                        .map(ScenarioConditionAvro::getSensorId),
                scenarioAddedEvent.getActions().stream()
                        .map(DeviceActionAvro::getSensorId)
        ).collect(Collectors.toSet());

        Map<String, Sensor> sensorsById = sensorRepository.findByIdIn(sensorIds)
                .stream()
                .collect(Collectors.toMap(
                        Sensor::getId,
                        Function.identity()
                ));

        for (ScenarioConditionAvro condition : scenarioAddedEvent.getConditions()) {

            Sensor sensor = sensorsById.get(condition.getSensorId());

            Condition conditionToSave = new Condition();
            conditionToSave.setType(condition.getType().name());
            conditionToSave.setOperation(condition.getOperation().name());
            switch (condition.getValue()) {
                case null -> conditionToSave.setValue(null);
                case Integer i -> conditionToSave.setValue(i);
                case Boolean b -> conditionToSave.setValue(b ? 1 : 0);
                default -> throw new IllegalArgumentException("Unsupported value type: " +
                        condition.getValue().getClass());
            }

            Condition conditionSaved = conditionRepository.save(conditionToSave);

            ScenarioCondition scenarioCondition = new ScenarioCondition();
            scenarioCondition.setId(
                    new ScenarioConditionId(
                            scenario.getId(),
                            sensor.getId(),
                            conditionSaved.getId()
                    )
            );

            scenarioCondition.setScenario(scenario);
            scenarioCondition.setCondition(conditionSaved);
            scenarioCondition.setSensor(sensor);

            scenarioConditionRepository.save(scenarioCondition);
        }

        for (DeviceActionAvro action : scenarioAddedEvent.getActions()) {
            Sensor sensor = sensorsById.get(action.getSensorId());

            Action actionToSave = new Action();
            actionToSave.setType(action.getType().name());
            actionToSave.setValue(action.getValue());

            Action actionSaved = actionRepository.save(actionToSave);

            ScenarioAction scenarioAction = new ScenarioAction();

            scenarioAction.setId(
                    new ScenarioActionId(
                            scenario.getId(),
                            sensor.getId(),
                            actionSaved.getId()
                    )
            );

            scenarioAction.setScenario(scenario);
            scenarioAction.setAction(actionSaved);
            scenarioAction.setSensor(sensor);

            scenarioActionRepository.save(scenarioAction);
        }
    }
}