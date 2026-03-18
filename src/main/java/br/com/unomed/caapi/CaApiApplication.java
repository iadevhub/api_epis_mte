package br.com.unomed.caapi;

import br.com.unomed.caapi.service.CaService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CaApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CaApiApplication.class, args);
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║   CA-API Unomed - Rodando!  🟢       ║");
        System.out.println("║   GET /ca/{numero}                   ║");
        System.out.println("║   GET /ca/reload?key=suasenha        ║");
        System.out.println("║   GET /ca/status                     ║");
        System.out.println("╚══════════════════════════════════════╝");
    }
}
