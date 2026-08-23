package ru.practicum.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Properties;

@Getter
@Setter
@ConfigurationProperties("kafka")
public class KafkaConfig {
    private Properties properties;
}