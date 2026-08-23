package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.Action;

public interface ActionRepository extends JpaRepository<Action, Long> {
}