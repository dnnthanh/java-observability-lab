package dev.lab.common;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LabHealthIndicators {
  @Bean
  HealthIndicator labReadiness(FaultState state) {
    return () -> state.ready() ? Health.up().withDetail("labReady", true).build()
        : Health.down().withDetail("labReady", false).build();
  }
  @Bean
  HealthIndicator labLiveness(FaultState state) {
    return () -> state.live() ? Health.up().withDetail("labLive", true).build()
        : Health.down().withDetail("labLive", false).build();
  }
}
