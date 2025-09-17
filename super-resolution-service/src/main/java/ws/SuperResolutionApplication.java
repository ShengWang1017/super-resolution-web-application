package ws;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class SuperResolutionApplication {

    public static void main(String[] args) {
        SpringApplication.run(SuperResolutionApplication.class, args);
    }

}
