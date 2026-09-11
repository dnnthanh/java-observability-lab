package dev.lab.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PaymentListener {
  private final ObjectMapper mapper;
  private final RestClient provider;
  private final JdbcTemplate jdbc;
  private final KafkaTemplate<String,String> kafka;

  public PaymentListener(ObjectMapper mapper, RestClient.Builder builder, JdbcTemplate jdbc,
      KafkaTemplate<String,String> kafka,
      @Value("${PROVIDER_URL:http://payment-provider-simulator:8080}") String providerUrl) {
    this.mapper=mapper; this.provider=builder.baseUrl(providerUrl).build(); this.jdbc=jdbc; this.kafka=kafka;
  }

  @KafkaListener(topics="orders.created", groupId="payment-worker")
  public void handle(String payload) throws Exception {
    JsonNode event=mapper.readTree(payload);
    String orderId=event.get("orderId").asText();
    long amount=event.get("amount").asLong();
    String status="SUCCEEDED";
    try {
      provider.post().uri("/api/provider/payments").contentType(MediaType.APPLICATION_JSON)
          .body(Map.of("orderId",orderId,"amount",amount)).retrieve().toBodilessEntity();
    } catch (Exception e) {
      status="FAILED";
    }
    jdbc.update("""
      insert into payments(order_id,amount,status,updated_at) values (?,?,?,now())
      on conflict(order_id) do update set status=excluded.status, updated_at=now()
      """, orderId, amount, status);
    kafka.send("payments.completed", orderId,
        mapper.writeValueAsString(Map.of("orderId",orderId,"status",status)));
  }
}
