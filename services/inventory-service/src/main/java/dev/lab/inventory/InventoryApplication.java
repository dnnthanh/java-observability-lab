package dev.lab.inventory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@SpringBootApplication(scanBasePackages = "dev.lab")
public class InventoryApplication {
  public static void main(String[] args) { SpringApplication.run(InventoryApplication.class, args); }
}
