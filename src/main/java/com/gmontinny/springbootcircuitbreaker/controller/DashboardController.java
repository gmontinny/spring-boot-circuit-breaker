package com.gmontinny.springbootcircuitbreaker.controller;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador que fornece um dashboard para visualizar o estado dos Circuit Breakers
 * e outros componentes de resiliência.
 */
@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public DashboardController(CircuitBreakerRegistry circuitBreakerRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    /**
     * Retorna o estado atual de todos os circuit breakers como JSON.
     * Isso pode ser usado por uma aplicação frontend para exibir o estado.
     *
     * @return Map contendo o estado de todos os circuit breakers
     */
    @GetMapping("/circuit-breakers")
    @ResponseBody
    public Map<String, Object> getCircuitBreakersState() {
        Map<String, Object> result = new HashMap<>();

        // Obter todos os circuit breakers
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(circuitBreaker -> {
            Map<String, Object> details = new HashMap<>();

            // Obter o estado do circuit breaker
            CircuitBreaker.State state = circuitBreaker.getState();
            details.put("state", state.name());

            // Obter as métricas do circuit breaker
            CircuitBreaker.Metrics metrics = circuitBreaker.getMetrics();
            details.put("failureRate", metrics.getFailureRate());
            details.put("slowCallRate", metrics.getSlowCallRate());
            details.put("numberOfSuccessfulCalls", metrics.getNumberOfSuccessfulCalls());
            details.put("numberOfFailedCalls", metrics.getNumberOfFailedCalls());
            details.put("numberOfSlowCalls", metrics.getNumberOfSlowCalls());
            details.put("numberOfNotPermittedCalls", metrics.getNumberOfNotPermittedCalls());

            result.put(circuitBreaker.getName(), details);
        });

        return result;
    }

    /**
     * Página de dashboard HTML simples que exibe o estado de todos os circuit breakers.
     *
     * @param model O modelo para adicionar atributos
     * @return O nome da view a ser renderizada
     */
    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("title", "Circuit Breaker Dashboard");
        return "dashboard";
    }
}
