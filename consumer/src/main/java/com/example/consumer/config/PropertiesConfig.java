package com.example.consumer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "properties")
@Data
// Make sure that Lombok does not generate any particular constructor for such a type
public class PropertiesConfig {
    private String connectString;
    private Integer connectionTimeout;
    private String registrationPath;
    private String to;
    private Integer sessionTimeout;
    private String username;
    private String password;
}
