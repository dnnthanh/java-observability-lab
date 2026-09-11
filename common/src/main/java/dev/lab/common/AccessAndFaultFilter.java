package dev.lab.common;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class AccessAndFaultFilter extends OncePerRequestFilter {
  private static final Logger log = LoggerFactory.getLogger(AccessAndFaultFilter.class);
  private final FaultState state;
  private final String service;
  private final String pod;

  public AccessAndFaultFilter(FaultState state,
      @Value("${spring.application.name:unknown}") String service,
      @Value("${POD_NAME:local}") String pod) {
    this.state = state; this.service = service; this.pod = pod;
  }

  @Override protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws ServletException, IOException {
    long start = System.nanoTime();
    String requestId = Optional.ofNullable(req.getHeader("X-Request-Id")).filter(s -> !s.isBlank())
        .orElse(UUID.randomUUID().toString());
    res.setHeader("X-Request-Id", requestId);
    res.setHeader("X-Service-Name", service);
    res.setHeader("X-Pod-Name", pod);
    MDC.put("requestId", requestId);
    try {
      if (!req.getRequestURI().startsWith("/actuator") && !req.getRequestURI().startsWith("/lab")) {
        long delay = state.delayMs();
        if (delay > 0) try { Thread.sleep(delay); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        if (state.shouldFail()) {
          res.sendError(503, "Injected lab failure");
          return;
        }
      }
      chain.doFilter(req, res);
    } finally {
      long ms = (System.nanoTime() - start) / 1_000_000;
      log.info("access service={} pod={} method={} path={} status={} duration_ms={} request_id={}",
          service, pod, req.getMethod(), req.getRequestURI(), res.getStatus(), ms, requestId);
      MDC.remove("requestId");
    }
  }
}
