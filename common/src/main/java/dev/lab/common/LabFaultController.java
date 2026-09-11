package dev.lab.common;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/lab/faults")
public class LabFaultController {
  private final FaultState state;
  public LabFaultController(FaultState state) { this.state = state; }

  @PostMapping("/delay")
  Map<String,Object> delay(@RequestParam long ms) {
    state.delayMs(ms); return status();
  }

  @PostMapping("/errors")
  Map<String,Object> errors(@RequestParam int percent) {
    state.errorPercent(percent); return status();
  }

  @PostMapping("/readiness")
  Map<String,Object> readiness(@RequestParam boolean up) {
    state.ready(up); return status();
  }

  @PostMapping("/liveness")
  Map<String,Object> liveness(@RequestParam boolean up) {
    state.live(up); return status();
  }

  @PostMapping("/memory")
  Map<String,Object> memory(@RequestParam int mb) {
    state.retainMegabytes(mb); return status();
  }

  @PostMapping("/memory/clear")
  Map<String,Object> clearMemory() {
    state.clearMemory(); return status();
  }

  @PostMapping("/cpu")
  Map<String,Object> cpu(@RequestParam(defaultValue = "15") int seconds) {
    Instant until = Instant.now().plusSeconds(Math.max(1, seconds));
    long value = 1;
    while (Instant.now().isBefore(until)) {
      value = value * 31 + System.nanoTime();
      if ((value & 1023) == 0) Thread.onSpinWait();
    }
    return Map.of("cpuBurnSeconds", seconds, "checksum", value);
  }

  @PostMapping("/reset")
  Map<String,Object> reset() {
    state.delayMs(0); state.errorPercent(0); state.ready(true); state.live(true); state.clearMemory();
    return status();
  }

  @GetMapping
  Map<String,Object> status() {
    return Map.of(
        "delayMs", state.delayMs(),
        "errorPercent", state.errorPercent(),
        "ready", state.ready(),
        "live", state.live(),
        "retainedBytes", state.retainedBytes());
  }
}
