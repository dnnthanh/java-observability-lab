package dev.lab.inventory;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
class InventoryApplicationTest {
  @Test void classIsPresent() { assertThat(InventoryApplication.class.getSimpleName()).isNotBlank(); }
}
