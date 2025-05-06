package com.gmontinny.springbootcircuitbreaker.controller;

import com.gmontinny.springbootcircuitbreaker.service.CircuitBreakerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

/**
 * Controlador REST que expõe endpoints para demonstrar
 * os diferentes padrões de resiliência do Resilience4J.
 */
@RestController
@RequestMapping("/api/resilience")
@Slf4j
public class ResilienceController {

    private final CircuitBreakerService circuitBreakerService;

    public ResilienceController(CircuitBreakerService circuitBreakerService) {
        this.circuitBreakerService = circuitBreakerService;
    }

    /**
     * Endpoint que demonstra o padrão Circuit Breaker.
     * Após várias falhas, o circuito será aberto e o fallback será utilizado.
     *
     * @return Resposta do serviço ou fallback
     */
    @GetMapping("/circuit-breaker")
    public String circuitBreakerDemo() {
        log.info("Circuit Breaker demo endpoint called");
        return circuitBreakerService.callWithCircuitBreaker();
    }

    /**
     * Endpoint que demonstra o padrão Retry.
     * Chamadas com falha serão repetidas de acordo com a configuração.
     *
     * @return Resposta do serviço ou fallback após as tentativas
     */
    @GetMapping("/retry")
    public String retryDemo() {
        log.info("Retry demo endpoint called");
        return circuitBreakerService.callWithRetry();
    }

    /**
     * Endpoint que demonstra o padrão Bulkhead.
     * Limita o número de chamadas concorrentes ao serviço.
     *
     * @return Resposta do serviço ou fallback se o bulkhead estiver cheio
     */
    @GetMapping("/bulkhead")
    public String bulkheadDemo() {
        log.info("Bulkhead demo endpoint called");
        return circuitBreakerService.callWithBulkhead();
    }

    /**
     * Endpoint que demonstra o padrão Rate Limiter.
     * Limita a taxa de chamadas ao serviço.
     *
     * @return Resposta do serviço ou fallback se o limite de taxa for excedido
     */
    @GetMapping("/rate-limiter")
    public String rateLimiterDemo() {
        log.info("Rate Limiter demo endpoint called");
        return circuitBreakerService.callWithRateLimiter();
    }

    /**
     * Endpoint que demonstra o padrão Time Limiter.
     * Define um tempo limite para chamadas ao serviço.
     *
     * @return Resposta do serviço ou fallback se o tempo limite for excedido
     */
    @GetMapping("/time-limiter")
    public CompletableFuture<String> timeLimiterDemo() {
        log.info("Time Limiter demo endpoint called");
        return circuitBreakerService.callWithTimeLimiter();
    }
}
