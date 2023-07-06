package com.example.provider.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "listener")
@Data
// Make sure that Lombok does not generate any particular constructor for such a type
public class PropertiesConfig {
    private String connectString;
    private String registrationPath;
    private String from;
    private String to;
}
