package dev.lab.edge;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

@RestController
@RequestMapping("/api")
public class EdgeController {
  private final RestClient client;
  public EdgeController(RestClient.Builder builder, @Value("${ORDER_URL:http://order-service:8080}") String orderUrl) {
    this.client = builder.baseUrl(orderUrl).build();
  }

  @PostMapping("/orders")
  ResponseEntity<String> create(@RequestBody String body, HttpServletRequest req) {
    String requestId = req.getHeader("X-Request-Id");
    String response = client.post().uri("/api/orders").contentType(MediaType.APPLICATION_JSON)
        .header("X-Request-Id", requestId == null ? "" : requestId)
        .body(body).retrieve().body(String.class);
    return ResponseEntity.status(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON).body(response);
  }

  @GetMapping("/orders/{id}")
  String get(@PathVariable String id) {
    return client.get().uri("/api/orders/{id}", id).retrieve().body(String.class);
  }

  @GetMapping("/whoami")
  Map<String,String> whoami(@Value("${POD_NAME:local}") String pod) {
    return Map.of("service","edge-service","pod",pod);
  }
}
