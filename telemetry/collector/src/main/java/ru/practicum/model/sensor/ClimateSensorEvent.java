package ru.practicum.model.sensor;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ClimateSensorEvent extends SensorEvent {
    private Integer temperatureC;
    private Integer humidity;
    private Integer co2Level;

    @Override
    public SensorEventType getType() {
        return SensorEventType.CLIMATE_SENSOR_EVENT;
    }
}
