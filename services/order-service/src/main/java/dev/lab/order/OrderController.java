package dev.lab.order;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
  private final OrderService service;
  public OrderController(OrderService service) { this.service=service; }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  Map<String,Object> create(@RequestBody OrderService.CreateOrder req) throws Exception { return service.create(req); }

  @GetMapping("/{id}")
  Map<String,Object> get(@PathVariable String id) { return service.get(id); }
}
