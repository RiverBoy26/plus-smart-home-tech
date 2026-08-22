package ru.practicum.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.model.sensor.*;
import ru.yandex.practicum.grpc.telemetry.messages.hub.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.messages.hub.scenario.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.messages.hub.scenario.ScenarioConditionProto;
import ru.yandex.practicum.grpc.telemetry.messages.sensor.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.hub.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.hub.device.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.hub.device.DeviceRemovedEventAvro;
import ru.yandex.practicum.kafka.telemetry.hub.device.DeviceTypeAvro;
import ru.yandex.practicum.kafka.telemetry.hub.scenario.*;
import ru.yandex.practicum.kafka.telemetry.sensor.*;

import java.time.Instant;
import java.util.List;

@UtilityClass
public class EventMapper {

    public HubEventAvro toAvroFromDeviceAddedProto(HubEventProto event) {
        DeviceAddedEventAvro deviceAddedEventAvro = DeviceAddedEventAvro.newBuilder()
                .setId(event.getDeviceAdded().getId())
                .setType(DeviceTypeAvro.valueOf(event.getDeviceAdded().getType().name()))
                .build();

        return HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(Instant.ofEpochSecond(event.getTimestamp().getSeconds(),
                        event.getTimestamp().getNanos()))
                .setPayload(deviceAddedEventAvro)
                .build();
    }

    public HubEventAvro toAvroFromDeviceRemovedProto(HubEventProto event) {
        DeviceRemovedEventAvro deviceRemovedEventAvro = DeviceRemovedEventAvro.newBuilder()
                .setId(event.getDeviceRemoved().getId())
                .build();

        return HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(Instant.ofEpochSecond(event.getTimestamp().getSeconds(),
                        event.getTimestamp().getNanos()))
                .setPayload(deviceRemovedEventAvro)
                .build();
    }

    public HubEventAvro toAvroFromScenarioAddedProto(HubEventProto event) {
        ScenarioAddedEventAvro scenarioAddedEventAvro = ScenarioAddedEventAvro.newBuilder()
                .setName(event.getScenarioAdded().getName())
                .setConditions(toScenarioConditions(event.getScenarioAdded().getConditionList()))
                .setActions(toDeviceActions(event.getScenarioAdded().getActionList()))
                .build();

        return HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(Instant.ofEpochSecond(event.getTimestamp().getSeconds(),
                        event.getTimestamp().getNanos()))
                .setPayload(scenarioAddedEventAvro)
                .build();
    }

    public HubEventAvro toAvroFromScenarioRemovedProto(HubEventProto event) {
        ScenarioRemovedEventAvro scenarioRemovedEventAvro = ScenarioRemovedEventAvro.newBuilder()
                .setName(event.getScenarioRemoved().getName())
                .build();

        return HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(Instant.ofEpochSecond(event.getTimestamp().getSeconds(),
                        event.getTimestamp().getNanos()))
                .setPayload(scenarioRemovedEventAvro)
                .build();
    }

    public SensorEventAvro toAvroFromMotionSensorProto(SensorEventProto event) {
        MotionSensorAvro motionSensorAvro = MotionSensorAvro.newBuilder()
                .setLinkQuality(event.getMotionSensor().getLinkQuality())
                .setMotion(event.getMotionSensor().getMotion())
                .setVoltage(event.getMotionSensor().getVoltage())
                .build();

        return SensorEventAvro.newBuilder()
                .setId(event.getId())
                .setHubId(event.getHubId())
                .setTimestamp(Instant.ofEpochSecond(event.getTimestamp().getSeconds(),
                        event.getTimestamp().getNanos()))
                .setPayload(motionSensorAvro)
                .build();
    }

    public SensorEventAvro toAvroFromTemperatureSensorProto(SensorEventProto event) {
        TemperatureSensorAvro temperatureSensorAvro = TemperatureSensorAvro.newBuilder()
                .setTemperatureC(event.getTemperatureSensor().getTemperatureC())
                .setTemperatureF(event.getTemperatureSensor().getTemperatureF())
                .build();

        return SensorEventAvro.newBuilder()
                .setId(event.getId())
                .setHubId(event.getHubId())
                .setTimestamp(Instant.ofEpochSecond(event.getTimestamp().getSeconds(),
                        event.getTimestamp().getNanos()))
                .setPayload(temperatureSensorAvro)
                .build();
    }

    public SensorEventAvro toAvroFromLightSensorProto(SensorEventProto event) {
        LightSensorAvro lightSensorAvro = LightSensorAvro.newBuilder()
                .setLinkQuality(event.getLightSensor().getLinkQuality())
                .setLuminosity(event.getLightSensor().getLuminosity())
                .build();

        return SensorEventAvro.newBuilder()
                .setId(event.getId())
                .setHubId(event.getHubId())
                .setTimestamp(Instant.ofEpochSecond(event.getTimestamp().getSeconds(),
                        event.getTimestamp().getNanos()))
                .setPayload(lightSensorAvro)
                .build();
    }

    public SensorEventAvro toAvroFromClimateSensorProto(SensorEventProto event) {
        ClimateSensorAvro climateSensorAvro = ClimateSensorAvro.newBuilder()
                .setTemperatureC(event.getClimateSensor().getTemperatureC())
                .setHumidity(event.getClimateSensor().getHumidity())
                .setCo2Level(event.getClimateSensor().getCo2Level())
                .build();

        return SensorEventAvro.newBuilder()
                .setId(event.getId())
                .setHubId(event.getHubId())
                .setTimestamp(Instant.ofEpochSecond(event.getTimestamp().getSeconds(),
                        event.getTimestamp().getNanos()))
                .setPayload(climateSensorAvro)
                .build();
    }

    public SensorEventAvro toAvroFromSwitchSensorProto(SensorEventProto event) {
        SwitchSensorAvro switchSensorAvro = SwitchSensorAvro.newBuilder()
                .setState(event.getSwitchSensor().getState())
                .build();

        return SensorEventAvro.newBuilder()
                .setId(event.getId())
                .setHubId(event.getHubId())
                .setTimestamp(Instant.ofEpochSecond(event.getTimestamp().getSeconds(),
                        event.getTimestamp().getNanos()))
                .setPayload(switchSensorAvro)
                .build();
    }

    private List<ScenarioConditionAvro> toScenarioConditions(List<ScenarioConditionProto> conditions) {
        return conditions.stream()
                .map(condition -> ScenarioConditionAvro.newBuilder()
                        .setSensorId(condition.getSensorId())
                        .setType(ConditionTypeAvro.valueOf(condition.getType().name()))
                        .setOperation(ConditionOperationAvro.valueOf(condition.getOperation().name()))
                        .setValue(condition.getValueCase() == ScenarioConditionProto.ValueCase.INT_VALUE ?
                                condition.getIntValue() : condition.getBoolValue())
                        .build())
                .toList();
    }

    private static List<DeviceActionAvro> toDeviceActions(List<DeviceActionProto> actions) {
        return actions.stream()
                .map(action -> DeviceActionAvro.newBuilder()
                        .setSensorId(action.getSensorId())
                        .setType(ActionTypeAvro.valueOf(action.getType().name()))
                        .setValue(action.getValue())
                        .build())
                .toList();
    }
}