package dev.lab.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

@Service
public class OrderService {
  private final JdbcTemplate jdbc;
  private final KafkaTemplate<String,String> kafka;
  private final ObjectMapper mapper;
  private final RestClient inventory;

  public OrderService(JdbcTemplate jdbc, KafkaTemplate<String,String> kafka, ObjectMapper mapper,
      RestClient.Builder builder, @Value("${INVENTORY_URL:http://inventory-service:8080}") String inventoryUrl) {
    this.jdbc=jdbc; this.kafka=kafka; this.mapper=mapper; this.inventory=builder.baseUrl(inventoryUrl).build();
  }

  public record CreateOrder(String customerId, String productCode, int quantity, long amount) {}

  @Transactional
  public Map<String,Object> create(CreateOrder req) throws Exception {
    String id="ORD-"+UUID.randomUUID().toString().substring(0,8).toUpperCase();
    inventory.post().uri("/api/inventory/reserve").contentType(MediaType.APPLICATION_JSON)
        .body(Map.of("productCode",req.productCode(),"quantity",req.quantity()))
        .retrieve().toBodilessEntity();
    jdbc.update("insert into orders(id,customer_id,product_code,quantity,amount,status,created_at) values (?,?,?,?,?,'PENDING',now())",
        id, req.customerId(), req.productCode(), req.quantity(), req.amount());
    String event=mapper.writeValueAsString(Map.of("orderId",id,"amount",req.amount(),"createdAt",Instant.now().toString()));
    kafka.send("orders.created", id, event);
    return get(id);
  }

  public Map<String,Object> get(String id) {
    return jdbc.queryForMap("select id,customer_id,product_code,quantity,amount,status,created_at from orders where id=?", id);
  }

  public void markPaid(String id, String paymentStatus) {
    jdbc.update("update orders set status=? where id=?", "SUCCEEDED".equals(paymentStatus) ? "CONFIRMED" : "PAYMENT_FAILED", id);
  }
}
