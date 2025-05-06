package com.gmontinny.springbootcircuitbreaker.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

/**
 * Este serviço demonstra o uso do padrão Circuit Breaker do Resilience4J
 * e outros padrões de resiliência para proteger chamadas ao serviço externo.
 */
@Service
@Slf4j
public class CircuitBreakerService {

    private final ExternalService externalService;

    // Constantes para as instâncias do circuit breaker
    private static final String CIRCUIT_BREAKER_INSTANCE = "externalServiceBreaker";
    private static final String RETRY_INSTANCE = "externalServiceRetry";
    private static final String BULKHEAD_INSTANCE = "externalServiceBulkhead";
    private static final String RATE_LIMITER_INSTANCE = "externalServiceRateLimiter";
    private static final String TIME_LIMITER_INSTANCE = "externalServiceTimeLimiter";

    public CircuitBreakerService(ExternalService externalService) {
        this.externalService = externalService;
    }

    /**
     * Chama o serviço externo com proteção do Circuit Breaker.
     * Se o circuito estiver aberto, o método de fallback será chamado.
     *
     * @return Resposta do serviço externo ou fallback
     */
    @CircuitBreaker(name = CIRCUIT_BREAKER_INSTANCE, fallbackMethod = "fallbackMethod")
    public String callWithCircuitBreaker() {
        log.info("Calling external service with Circuit Breaker protection");
        return externalService.callExternalService();
    }

    /**
     * Chama o serviço externo com proteção de Retry.
     * Se todas as tentativas falharem, o método de fallback será chamado.
     *
     * @return Resposta do serviço externo ou fallback
     */
    @Retry(name = RETRY_INSTANCE, fallbackMethod = "fallbackMethod")
    public String callWithRetry() {
        log.info("Calling external service with Retry protection");
        return externalService.callExternalService();
    }

    /**
     * Chama o serviço externo com proteção de Bulkhead.
     * Se o bulkhead estiver cheio, o método de fallback será chamado.
     *
     * @return Resposta do serviço externo ou fallback
     */
    @Bulkhead(name = BULKHEAD_INSTANCE, fallbackMethod = "fallbackMethod")
    public String callWithBulkhead() {
        log.info("Calling external service with Bulkhead protection");
        return externalService.callExternalService();
    }

    /**
     * Chama o serviço externo com proteção de Rate Limiter.
     * Se o limite de taxa for excedido, o método de fallback será chamado.
     *
     * @return Resposta do serviço externo ou fallback
     */
    @RateLimiter(name = RATE_LIMITER_INSTANCE, fallbackMethod = "fallbackMethod")
    public String callWithRateLimiter() {
        log.info("Calling external service with Rate Limiter protection");
        return externalService.callExternalService();
    }

    /**
     * Chama o serviço externo com proteção de Time Limiter.
     * Se a chamada demorar muito, o método de fallback será chamado.
     *
     * @return CompletableFuture com resposta do serviço externo ou fallback
     */
    @TimeLimiter(name = TIME_LIMITER_INSTANCE, fallbackMethod = "timeLimiterFallback")
    public CompletableFuture<String> callWithTimeLimiter() {
        log.info("Calling external service with Time Limiter protection");
        return CompletableFuture.supplyAsync(() -> externalService.callExternalService());
    }

    /**
     * Método de fallback para Circuit Breaker, Retry, Bulkhead e Rate Limiter.
     *
     * @param e A exceção que acionou o fallback
     * @return Resposta de fallback
     */
    private String fallbackMethod(Exception e) {
        log.error("Fallback method called due to: {}", e.getMessage());
        return "Fallback response: The service is currently unavailable. Please try again later.";
    }

    /**
     * Método de fallback específico para Time Limiter.
     *
     * @param e A exceção que acionou o fallback
     * @return CompletableFuture com resposta de fallback
     */
    private CompletableFuture<String> timeLimiterFallback(Exception e) {
        log.error("Time Limiter fallback method called due to: {}", e.getMessage());
        return CompletableFuture.supplyAsync(() -> 
            "Fallback response: The service took too long to respond. Please try again later.");
    }

    /**
     * Método de fallback para TimeoutException no Time Limiter.
     *
     * @param e A TimeoutException que acionou o fallback
     * @return CompletableFuture com resposta de fallback
     */
    private CompletableFuture<String> timeLimiterFallback(TimeoutException e) {
        log.error("Time Limiter fallback method called due to timeout: {}", e.getMessage());
        return CompletableFuture.supplyAsync(() -> 
            "Fallback response: The service request timed out. Please try again later.");
    }
}
