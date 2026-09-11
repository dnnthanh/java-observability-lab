package dev.lab.provider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@SpringBootApplication(scanBasePackages = "dev.lab")
public class PaymentProviderApplication {
  public static void main(String[] args) { SpringApplication.run(PaymentProviderApplication.class, args); }
}
