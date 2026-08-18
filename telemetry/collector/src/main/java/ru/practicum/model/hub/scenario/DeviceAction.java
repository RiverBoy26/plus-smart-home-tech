package ru.practicum.model.hub.scenario;

import lombok.Data;

@Data
public class DeviceAction {
    private String sensorId;
    private DeviceActionType type;
    private Integer value;
}