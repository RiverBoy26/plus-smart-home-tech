package ru.practicum.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Properties;

@Getter
@Setter
@ConfigurationProperties(prefix = "kafka.snapshots")
public class SnapshotKafkaConfig {
    private Properties properties = new Properties();
}