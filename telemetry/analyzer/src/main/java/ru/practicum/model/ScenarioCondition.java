package ru.practicum.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "scenario_conditions")
@Data
@NoArgsConstructor
public class ScenarioCondition {
    @EmbeddedId
    private ScenarioConditionId id;

    @ManyToOne
    @MapsId("scenarioId")
    @JoinColumn(name = "scenario_id")
    private Scenario scenario;


    @ManyToOne
    @MapsId("conditionId")
    @JoinColumn(name = "condition_id")
    private Condition condition;

    @ManyToOne
    @MapsId("sensorId")
    @JoinColumn(name = "sensor_id")
    private Sensor sensor;
}