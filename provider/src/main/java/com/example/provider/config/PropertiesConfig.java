package com.example.provider.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

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
    //    private List<AuthInfoConfig> configs;

//    public static class AuthInfoConfig {
//
//        private String scheme;
//        private String credentials;
//
//        public String getScheme() {
//            return scheme;
//        }
//
//        public void setScheme(String scheme) {
//            this.scheme = scheme;
//        }
//
//        public String getCredentials() {
//            return credentials;
//        }
//
//        public void setCredentials(String credentials) {
//            this.credentials = credentials;
//        }
//    }
}
