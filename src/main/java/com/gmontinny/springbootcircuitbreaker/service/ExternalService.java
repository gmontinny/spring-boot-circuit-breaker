package com.gmontinny.springbootcircuitbreaker.service;

import org.springframework.stereotype.Service;
import java.util.Random;

/**
 * Este serviço simula um serviço externo que pode falhar ocasionalmente.
 * É usado para demonstrar o padrão Circuit Breaker.
 */
@Service
public class ExternalService {

    private final Random random = new Random();

    /**
     * Simula uma chamada para um serviço externo que pode falhar.
     * 
     * @return Uma resposta do serviço externo
     * @throws RuntimeException se o serviço falhar (falha simulada)
     */
    public String callExternalService() {
        // Simula um atraso (0-2 segundos)
        try {
            Thread.sleep(random.nextInt(2000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Simula uma falha com 50% de probabilidade
        if (random.nextBoolean()) {
            throw new RuntimeException("External service failed!");
        }

        return "Response from external service";
    }
}
