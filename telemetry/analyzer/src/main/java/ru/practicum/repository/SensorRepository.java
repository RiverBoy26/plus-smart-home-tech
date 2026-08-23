package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.Sensor;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface SensorRepository extends JpaRepository<Sensor, String> {
    boolean existsByIdInAndHubId(Collection<String> ids, String hubId);

    Optional<Sensor> findByIdAndHubId(String id, String hubId);

    List<Sensor> findByIdIn(Set<String> ids);
}