package dev.lab.provider;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/provider")
public class ProviderController {
  public record PaymentRequest(String orderId, long amount) {}

  @PostMapping("/payments")
  Map<String,Object> pay(@RequestBody PaymentRequest request) {
    return Map.of("providerPaymentId","PAY-"+UUID.randomUUID().toString().substring(0,8),
        "orderId",request.orderId(),"status","SUCCEEDED","processedAt",Instant.now().toString());
  }
}
