package com.smartgate.backend.intercom;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "smartgate.intercom")
public record IntercomProperties(
    String host,
    int port
) {
}

