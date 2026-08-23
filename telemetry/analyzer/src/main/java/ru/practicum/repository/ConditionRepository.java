package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.Condition;

public interface ConditionRepository extends JpaRepository<Condition, Long> {
}