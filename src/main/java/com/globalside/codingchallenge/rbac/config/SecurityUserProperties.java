package com.globalside.codingchallenge.rbac.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@ConfigurationProperties(prefix = "security")
public record SecurityUserProperties(
        @Valid @NotEmpty List<User> users
) {
    public record User(
            @NotBlank String username,
            @NotBlank String password,
            @NotEmpty List<@NotBlank String> roles
    ) {
    }
}