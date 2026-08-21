package ru.practicum.model.hub.scenario;

import lombok.Data;

@Data
public class ScenarioCondition {
    private String sensorId;
    private ConditionType type;
    private OperationType operation;
    private Integer value;
}