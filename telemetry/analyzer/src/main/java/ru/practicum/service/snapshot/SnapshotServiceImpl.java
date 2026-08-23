package ru.practicum.service.snapshot;

import com.google.protobuf.Timestamp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.model.Scenario;
import ru.practicum.model.ScenarioAction;
import ru.practicum.model.ScenarioCondition;
import ru.practicum.repository.ScenarioActionRepository;
import ru.practicum.repository.ScenarioConditionRepository;
import ru.practicum.repository.ScenarioRepository;
import ru.practicum.service.grpc.HubActionProducer;
import ru.practicum.service.snapshot.handler.ConditionHandler;
import ru.yandex.practicum.grpc.telemetry.messages.hub.DeviceActionRequestProto;
import ru.yandex.practicum.grpc.telemetry.messages.hub.scenario.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.messages.hub.scenario.DeviceActionProto;
import ru.yandex.practicum.kafka.telemetry.snapshot.SensorsSnapshotAvro;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@Slf4j
public class SnapshotServiceImpl implements SnapshotService {
    private final HubActionProducer hubActionProducer;
    private final ScenarioConditionRepository scenarioConditionRepository;
    private final ScenarioRepository scenarioRepository;
    private final Map<String, ConditionHandler> conditionHandlers;
    private final ScenarioActionRepository scenarioActionRepository;

    public SnapshotServiceImpl(
            ScenarioConditionRepository scenarioConditionRepository,
            List<ConditionHandler> conditionStrategies,
            ScenarioRepository scenarioRepository,
            HubActionProducer hubActionProducer, ScenarioActionRepository scenarioActionRepository) {

        this.scenarioConditionRepository = scenarioConditionRepository;
        this.scenarioRepository = scenarioRepository;
        this.hubActionProducer = hubActionProducer;

        this.conditionHandlers = conditionStrategies.stream()
                .collect(Collectors.toMap(
                        ConditionHandler::getType,
                        Function.identity()
                ));
        this.scenarioActionRepository = scenarioActionRepository;
    }

    @Override
    public void handleSnapshot(SensorsSnapshotAvro sensorsSnapshotAvro) {
        List<Scenario> scenarios = scenarioRepository.findByHubId(sensorsSnapshotAvro.getHubId());
        List<ScenarioCondition> conditions = scenarioConditionRepository
                .findByScenario_HubId(sensorsSnapshotAvro.getHubId());
        List<ScenarioAction> actions = scenarioActionRepository
                .findByScenario_HubId(sensorsSnapshotAvro.getHubId());

        log.info(
                "Snapshot hub={}, scenarios={}, conditions={}, actions={}",
                sensorsSnapshotAvro.getHubId(),
                scenarios.size(),
                conditions.size(),
                actions.size()
        );

        for (Scenario scenario : scenarios) {

            List<ScenarioCondition> scenarioConditions = conditions.stream()
                    .filter(condition -> condition.getScenario().getId().equals(scenario.getId()))
                    .toList();

            boolean isTriggered = scenarioConditions
                    .stream()
                    .allMatch(condition ->
                            checkCondition(condition, sensorsSnapshotAvro));

            Timestamp timestamp = Timestamp.newBuilder()
                    .setSeconds(sensorsSnapshotAvro.getTimestamp().getEpochSecond())
                    .setNanos(sensorsSnapshotAvro.getTimestamp().getNano())
                    .build();

            List<ScenarioAction> scenarioActions = actions.stream()
                    .filter(a -> a.getScenario().getId().equals(scenario.getId()))
                    .toList();

            if (isTriggered) {
                for (ScenarioAction action : scenarioActions) {

                    DeviceActionProto actionProto = DeviceActionProto.newBuilder()
                            .setSensorId(action.getSensor().getId())
                            .setType(ActionTypeProto.valueOf(action.getAction().getType()))
                            .setValue(action.getAction().getValue())
                            .build();

                    hubActionProducer.sendAction(
                            DeviceActionRequestProto.newBuilder()
                                    .setHubId(scenario.getHubId())
                                    .setScenarioName(scenario.getName())
                                    .setAction(actionProto)
                                    .setTimestamp(timestamp)
                                    .build()
                    );
                }
            }
        }
    }

    private boolean checkCondition(ScenarioCondition condition, SensorsSnapshotAvro sensorsSnapshotAvro) {

        String type = condition.getCondition().getType();

        ConditionHandler conditionHandler = conditionHandlers.get(type);

        if (conditionHandler == null) {
            throw new IllegalArgumentException("Unsupported condition type: " + type);
        }

        return conditionHandler.check(condition, sensorsSnapshotAvro);
    }
}