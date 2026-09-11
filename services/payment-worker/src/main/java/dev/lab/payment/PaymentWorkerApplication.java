package dev.lab.payment;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@SpringBootApplication(scanBasePackages = "dev.lab")
public class PaymentWorkerApplication {
  public static void main(String[] args) { SpringApplication.run(PaymentWorkerApplication.class, args); }
}
