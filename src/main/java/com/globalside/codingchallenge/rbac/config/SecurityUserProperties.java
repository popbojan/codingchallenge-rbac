package com.globalside.codingchallenge.rbac.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "security")
public record SecurityUserProperties(List<User> users) {

    public record User(String username, String password, List<String> roles) {
    }
}
