package com.iot_sw.iot_web_backend.Auth.service;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(10);

    private final Map<String, AttemptInfo> attempts = new ConcurrentHashMap<>();

    public boolean isBlocked(String key) {
        AttemptInfo info = attempts.get(key);
        if (info == null) {
            return false;
        }

        if (info.lockedUntil != null && info.lockedUntil.isAfter(Instant.now())) {
            return true;
        }

        if (info.lockedUntil != null && !info.lockedUntil.isAfter(Instant.now())) {
            attempts.remove(key);
        }

        return false;
    }

    public void recordFailure(String key) {
        attempts.compute(key, (ignored, current) -> {
            AttemptInfo info = current == null ? new AttemptInfo() : current;
            info.count++;

            if (info.count >= MAX_ATTEMPTS) {
                info.lockedUntil = Instant.now().plus(LOCK_DURATION);
            }

            return info;
        });
    }

    public void clear(String key) {
        attempts.remove(key);
    }

    private static final class AttemptInfo {
        private int count;
        private Instant lockedUntil;
    }
}
