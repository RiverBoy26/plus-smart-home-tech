package ru.practicum.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "kafka.common")
public class KafkaConfig {
    private Duration pollTimeout = Duration.ofSeconds(1);
    private int commitBatchSize = 10;
}