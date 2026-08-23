package ru.practicum.service.hub;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.service.hub.handler.HubEventHandler;
import ru.yandex.practicum.kafka.telemetry.hub.HubEventAvro;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class HubEventServiceImpl implements HubEventService {
    private final Map<Class<?>, HubEventHandler> hubEventHandlers;

    public HubEventServiceImpl(Set<HubEventHandler> hubEventHandlers) {
        this.hubEventHandlers = hubEventHandlers.stream()
                .collect(Collectors.toMap(
                        HubEventHandler::getEventType,
                        Function.identity()
                ));
    }

    @Override
    public void handleHubEvent(HubEventAvro event) {
        if (hubEventHandlers.containsKey(event.getPayload().getClass())) {
            hubEventHandlers.get(event.getPayload().getClass()).handle(event);
        } else {
            throw new IllegalArgumentException("HubEventHandler not found for type: "
                    + event.getPayload().getClass());
        }
    }
}