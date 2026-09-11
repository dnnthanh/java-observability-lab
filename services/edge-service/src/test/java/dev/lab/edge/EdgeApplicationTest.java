package dev.lab.edge;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
class EdgeApplicationTest {
  @Test void classIsPresent() { assertThat(EdgeApplication.class.getSimpleName()).isNotBlank(); }
}
