package dev.lab.common;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
class FaultStateTest {
  @Test void clampsErrorPercentageAndCanResetMemory() {
    FaultState s = new FaultState();
    s.errorPercent(120);
    assertThat(s.errorPercent()).isEqualTo(100);
    s.retainMegabytes(1);
    assertThat(s.retainedBytes()).isGreaterThanOrEqualTo(1024L * 1024);
    s.clearMemory();
    assertThat(s.retainedBytes()).isZero();
  }
}
