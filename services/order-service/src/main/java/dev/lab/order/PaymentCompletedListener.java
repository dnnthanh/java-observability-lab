package dev.lab.order;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentCompletedListener {
  private final ObjectMapper mapper;
  private final OrderService service;
  public PaymentCompletedListener(ObjectMapper mapper, OrderService service) { this.mapper=mapper; this.service=service; }

  @KafkaListener(topics="payments.completed", groupId="order-status")
  public void handle(String payload) throws Exception {
    JsonNode n=mapper.readTree(payload);
    service.markPaid(n.get("orderId").asText(), n.get("status").asText());
  }
}
