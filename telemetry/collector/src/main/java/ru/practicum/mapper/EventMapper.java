package ru.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.model.hub.HubEvent;
import ru.practicum.model.hub.device.DeviceAddedEvent;
import ru.practicum.model.hub.device.DeviceRemovedEvent;
import ru.practicum.model.hub.scenario.DeviceAction;
import ru.practicum.model.hub.scenario.ScenarioAddedEvent;
import ru.practicum.model.hub.scenario.ScenarioCondition;
import ru.practicum.model.hub.scenario.ScenarioRemovedEvent;
import ru.practicum.model.sensor.*;
import ru.yandex.practicum.kafka.telemetry.hub.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.hub.device.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.hub.device.DeviceRemovedEventAvro;
import ru.yandex.practicum.kafka.telemetry.hub.device.DeviceTypeAvro;
import ru.yandex.practicum.kafka.telemetry.hub.scenario.*;
import ru.yandex.practicum.kafka.telemetry.sensor.*;

import java.util.List;

@Component
public class EventMapper {
    public static HubEventAvro toHubEventAvro(HubEvent event) {

        switch (event.getType()) {
            case DEVICE_ADDED:
                DeviceAddedEvent deviceAddedEvent = (DeviceAddedEvent) event;

                DeviceAddedEventAvro deviceAddedEventAvro = DeviceAddedEventAvro.newBuilder()
                        .setId(deviceAddedEvent.getId())
                        .setType(DeviceTypeAvro.valueOf(deviceAddedEvent.getDeviceType().name()))
                        .build();

                return HubEventAvro.newBuilder()
                        .setHubId(event.getHubId())
                        .setTimestamp(event.getTimestamp())
                        .setPayload(deviceAddedEventAvro)
                        .build();
            case DEVICE_REMOVED:
                DeviceRemovedEvent deviceRemovedEvent = (DeviceRemovedEvent) event;

                DeviceRemovedEventAvro deviceRemovedEventAvro = DeviceRemovedEventAvro.newBuilder()
                        .setId(deviceRemovedEvent.getId())
                        .build();

                return HubEventAvro.newBuilder()
                        .setHubId(event.getHubId())
                        .setTimestamp(event.getTimestamp())
                        .setPayload(deviceRemovedEventAvro)
                        .build();
            case SCENARIO_ADDED:
                ScenarioAddedEvent scenarioAddedEvent = (ScenarioAddedEvent) event;

                ScenarioAddedEventAvro scenarioAddedEventAvro = ScenarioAddedEventAvro.newBuilder()
                        .setName(scenarioAddedEvent.getName())
                        .setConditions(toScenarioConditions(scenarioAddedEvent.getConditions()))
                        .setActions(toDeviceActions(scenarioAddedEvent.getActions()))
                        .build();

                return HubEventAvro.newBuilder()
                        .setHubId(event.getHubId())
                        .setTimestamp(event.getTimestamp())
                        .setPayload(scenarioAddedEventAvro)
                        .build();
            case SCENARIO_REMOVED:
                ScenarioRemovedEvent scenarioRemovedEvent = (ScenarioRemovedEvent) event;

                ScenarioRemovedEventAvro scenarioRemovedEventAvro = ScenarioRemovedEventAvro.newBuilder()
                        .setName(scenarioRemovedEvent.getName())
                        .build();

                return HubEventAvro.newBuilder()
                        .setHubId(event.getHubId())
                        .setTimestamp(event.getTimestamp())
                        .setPayload(scenarioRemovedEventAvro)
                        .build();
            default:
                throw new IllegalArgumentException("Unknown HubEvent type: " + event.getType());
        }
    }

    private static List<ScenarioConditionAvro> toScenarioConditions(List<ScenarioCondition> conditions) {
        return conditions.stream()
                .map(condition -> ScenarioConditionAvro.newBuilder()
                        .setSensorId(condition.getSensorId())
                        .setType(ConditionTypeAvro.valueOf(condition.getType().name()))
                        .setOperation(ConditionOperationAvro.valueOf(condition.getOperation().name()))
                        .setValue(condition.getValue())
                        .build())
                .toList();
    }

    private static List<DeviceActionAvro> toDeviceActions(List<DeviceAction> actions) {
        return actions.stream()
                .map(action -> DeviceActionAvro.newBuilder()
                        .setSensorId(action.getSensorId())
                        .setType(ActionTypeAvro.valueOf(action.getType().name()))
                        .setValue(action.getValue())
                        .build())
                .toList();
    }

    public static SensorEventAvro toSensorEventAvro(SensorEvent event) {

        switch (event.getType()) {
            case MOTION_SENSOR_EVENT:
                MotionSensorEvent motionSensorEvent = (MotionSensorEvent) event;

                MotionSensorAvro motionSensorAvro = MotionSensorAvro.newBuilder()
                        .setLinkQuality(motionSensorEvent.getLinkQuality())
                        .setMotion(motionSensorEvent.getMotion())
                        .setVoltage(motionSensorEvent.getVoltage())
                        .build();

                return SensorEventAvro.newBuilder()
                        .setId(event.getId())
                        .setHubId(event.getHubId())
                        .setTimestamp(event.getTimestamp())
                        .setPayload(motionSensorAvro)
                        .build();
            case TEMPERATURE_SENSOR_EVENT:
                TemperatureSensorEvent temperatureSensorEvent = (TemperatureSensorEvent) event;

                TemperatureSensorAvro temperatureSensorAvro = TemperatureSensorAvro.newBuilder()
                        .setTemperatureC(temperatureSensorEvent.getTemperatureC())
                        .setTemperatureF(temperatureSensorEvent.getTemperatureF())
                        .build();

                return SensorEventAvro.newBuilder()
                        .setId(event.getId())
                        .setHubId(event.getHubId())
                        .setTimestamp(event.getTimestamp())
                        .setPayload(temperatureSensorAvro)
                        .build();
            case LIGHT_SENSOR_EVENT:
                LightSensorEvent lightSensorEvent = (LightSensorEvent) event;

                LightSensorAvro lightSensorAvro = LightSensorAvro.newBuilder()
                        .setLinkQuality(lightSensorEvent.getLinkQuality())
                        .setLuminosity(lightSensorEvent.getLuminosity())
                        .build();

                return SensorEventAvro.newBuilder()
                        .setId(event.getId())
                        .setHubId(event.getHubId())
                        .setTimestamp(event.getTimestamp())
                        .setPayload(lightSensorAvro)
                        .build();
            case CLIMATE_SENSOR_EVENT:
                ClimateSensorEvent climateSensorEvent = (ClimateSensorEvent) event;

                ClimateSensorAvro climateSensorAvro = ClimateSensorAvro.newBuilder()
                        .setTemperatureC(climateSensorEvent.getTemperatureC())
                        .setHumidity(climateSensorEvent.getHumidity())
                        .setCo2Level(climateSensorEvent.getCo2Level())
                        .build();

                return SensorEventAvro.newBuilder()
                        .setId(event.getId())
                        .setHubId(event.getHubId())
                        .setTimestamp(event.getTimestamp())
                        .setPayload(climateSensorAvro)
                        .build();
            case SWITCH_SENSOR_EVENT:
                SwitchSensorEvent switchSensorEvent = (SwitchSensorEvent) event;

                SwitchSensorAvro switchSensorAvro = SwitchSensorAvro.newBuilder()
                        .setState(switchSensorEvent.getState())
                        .build();

                return SensorEventAvro.newBuilder()
                        .setId(event.getId())
                        .setHubId(event.getHubId())
                        .setTimestamp(event.getTimestamp())
                        .setPayload(switchSensorAvro)
                        .build();
            default:
                throw new IllegalArgumentException("Неизвестный тип SensorEvent: " + event.getType());
        }
    }
}