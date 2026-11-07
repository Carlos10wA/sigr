package co.edu.tecnologicaoriente.sigr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SigrApplication {
    public static void main(String[] args) {
        SpringApplication.run(SigrApplication.class, args);
    }
}
