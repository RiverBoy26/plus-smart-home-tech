package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.ScenarioCondition;

import java.util.List;

public interface ScenarioConditionRepository extends JpaRepository<ScenarioCondition, Long> {
    List<ScenarioCondition> findByScenario_HubId(String hubId);
}