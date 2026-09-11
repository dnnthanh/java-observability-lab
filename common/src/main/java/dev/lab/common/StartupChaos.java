package dev.lab.common;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StartupChaos implements InitializingBean {
  @Value("${LAB_STARTUP_DELAY_MS:0}") long startupDelayMs;
  @Value("${LAB_CRASH_ON_START:false}") boolean crashOnStart;

  @Override public void afterPropertiesSet() throws Exception {
    if (startupDelayMs > 0) Thread.sleep(startupDelayMs);
    if (crashOnStart) throw new IllegalStateException("LAB_CRASH_ON_START=true");
  }
}
