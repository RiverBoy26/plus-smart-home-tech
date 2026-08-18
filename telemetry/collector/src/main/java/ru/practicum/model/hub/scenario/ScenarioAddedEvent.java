package ru.practicum.model.hub.scenario;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.practicum.model.hub.HubEvent;
import ru.practicum.model.hub.HubEventType;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScenarioAddedEvent extends HubEvent {
    private String name;
    private List<ScenarioCondition> conditions;
    private List<DeviceAction> actions;

    @Override
    public HubEventType getType() {
        return HubEventType.SCENARIO_ADDED;
    }
}