package dev.lab.inventory;

import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {
  private final JdbcTemplate jdbc;
  public InventoryService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

  @Transactional
  public Map<String,Object> reserve(String productCode, int quantity) {
    Integer available = jdbc.queryForObject(
        "select quantity from inventory where product_code=? for update", Integer.class, productCode);
    if (available == null || available < quantity) throw new IllegalStateException("insufficient stock");
    jdbc.update("update inventory set quantity=quantity-? where product_code=?", quantity, productCode);
    return Map.of("productCode", productCode, "reserved", quantity, "remaining", available - quantity);
  }

  @Transactional
  public void holdLock(String productCode, int seconds) {
    jdbc.queryForObject("select quantity from inventory where product_code=? for update", Integer.class, productCode);
    jdbc.queryForList("select pg_sleep(?)", seconds);
  }

  public void slowQuery(int seconds) {
    jdbc.queryForList("select pg_sleep(?)", seconds);
  }

  public void holdConnection(int seconds) {
    jdbc.queryForList("select pg_sleep(?)", seconds);
  }
}
