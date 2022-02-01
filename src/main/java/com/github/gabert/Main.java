package com.github.gabert;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Main {
    public static void main(String[] args) {
        RateLimiter rateLimiter = new RateLimiter(1, 1, ChronoUnit.SECONDS);

        for (int i = 0; i < 10; i++) {
            if ( rateLimiter.permit() ) {
                System.out.println(LocalDateTime.now() + ": #");
            }
        }
    }
}
