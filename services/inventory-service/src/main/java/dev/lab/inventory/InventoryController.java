package dev.lab.inventory;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class InventoryController {
  private final InventoryService service;
  public InventoryController(InventoryService service) { this.service = service; }

  public record ReserveRequest(String productCode, int quantity) {}

  @PostMapping("/api/inventory/reserve")
  @ResponseStatus(HttpStatus.OK)
  Map<String,Object> reserve(@RequestBody ReserveRequest request) {
    return service.reserve(request.productCode(), request.quantity());
  }

  @PostMapping("/lab/db/slow")
  Map<String,Object> slow(@RequestParam(defaultValue="5") int seconds) {
    service.slowQuery(seconds); return Map.of("slept", seconds);
  }

  @PostMapping("/lab/db/hold-connection")
  Map<String,Object> holdConnection(@RequestParam(defaultValue="10") int seconds) {
    service.holdConnection(seconds); return Map.of("heldSeconds", seconds);
  }

  @PostMapping("/lab/db/lock")
  Map<String,Object> lock(@RequestParam(defaultValue="SKU-1") String product,
                          @RequestParam(defaultValue="20") int seconds) {
    service.holdLock(product, seconds); return Map.of("locked", product, "seconds", seconds);
  }
}
