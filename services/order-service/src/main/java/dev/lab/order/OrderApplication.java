package dev.lab.order;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@SpringBootApplication(scanBasePackages = "dev.lab")
public class OrderApplication {
  public static void main(String[] args) { SpringApplication.run(OrderApplication.class, args); }
}
