package dev.lab.payment;

import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
  private final JdbcTemplate jdbc;
  public PaymentController(JdbcTemplate jdbc) { this.jdbc=jdbc; }
  @GetMapping("/{orderId}")
  Map<String,Object> get(@PathVariable String orderId) {
    return jdbc.queryForMap("select order_id,amount,status,updated_at from payments where order_id=?", orderId);
  }
}
