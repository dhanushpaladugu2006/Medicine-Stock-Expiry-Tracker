package com.medicinetracker.config;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        Jwt jwt,
        Storage storage,
        Notification notification,
        Cors cors,
        Scheduler scheduler
) {
    public record Jwt(String secret, long expirationMinutes) {
    }

    public record Storage(String uploadDir, long maxFileSizeMb) {
    }

    public record Notification(String senderEmail, boolean enableEmail, boolean enableSmsPlaceholder) {
    }

    public record Cors(List<String> allowedOrigins) {
        public Cors {
            allowedOrigins = allowedOrigins == null ? new ArrayList<>() : allowedOrigins;
        }
    }

    public record Scheduler(String zoneId) {
        public ZoneId toZoneId() {
            return zoneId == null || zoneId.isBlank() ? ZoneId.systemDefault() : ZoneId.of(zoneId);
        }
    }
}
