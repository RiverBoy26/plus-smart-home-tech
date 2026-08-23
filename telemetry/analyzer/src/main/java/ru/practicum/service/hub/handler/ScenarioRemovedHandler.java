package ru.practicum.service.hub.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.model.Scenario;
import ru.practicum.repository.ScenarioRepository;
import ru.yandex.practicum.kafka.telemetry.hub.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.hub.scenario.ScenarioRemovedEventAvro;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ScenarioRemovedHandler implements HubEventHandler {
    private final ScenarioRepository scenarioRepository;

    @Override
    public Class<?> getEventType() {
        return ScenarioRemovedEventAvro.class;
    }

    @Override
    public void handle(HubEventAvro event) {
        ScenarioRemovedEventAvro scenarioRemovedEvent = (ScenarioRemovedEventAvro) event.getPayload();

        Optional<Scenario> scenario = scenarioRepository.findByHubIdAndName(
                event.getHubId(),
                scenarioRemovedEvent.getName()
        );

        scenario.ifPresent(scenarioRepository::delete);
    }
}