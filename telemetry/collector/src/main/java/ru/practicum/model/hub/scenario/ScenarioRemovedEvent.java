package ru.practicum.model.hub.scenario;

import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.practicum.model.hub.HubEvent;
import ru.practicum.model.hub.HubEventType;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScenarioRemovedEvent extends HubEvent {
    @Size(min = 3, message = "Длина строки должна быть от 3 до 2147483647 символов")
    private String name;

    @Override
    public HubEventType getType() {
        return HubEventType.SCENARIO_REMOVED;
    }
}