package dev.lab.common;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

@Component
public class FaultState {
  private final AtomicLong delayMs = new AtomicLong();
  private final AtomicInteger errorPercent = new AtomicInteger();
  private final AtomicBoolean ready = new AtomicBoolean(true);
  private final AtomicBoolean live = new AtomicBoolean(true);
  private final List<byte[]> retained = new CopyOnWriteArrayList<>();

  public long delayMs() { return delayMs.get(); }
  public void delayMs(long value) { delayMs.set(Math.max(0, value)); }
  public int errorPercent() { return errorPercent.get(); }
  public void errorPercent(int value) { errorPercent.set(Math.max(0, Math.min(100, value))); }
  public boolean ready() { return ready.get(); }
  public void ready(boolean value) { ready.set(value); }
  public boolean live() { return live.get(); }
  public void live(boolean value) { live.set(value); }
  public boolean shouldFail() { return ThreadLocalRandom.current().nextInt(100) < errorPercent.get(); }
  public long retainedBytes() { return retained.stream().mapToLong(a -> a.length).sum(); }
  public void retainMegabytes(int mb) {
    for (int i = 0; i < mb; i++) retained.add(new byte[1024 * 1024]);
  }
  public void clearMemory() { retained.clear(); }
}
