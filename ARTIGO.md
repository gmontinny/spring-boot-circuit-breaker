# Implementando Padrões de Resiliência com Spring Boot e Resilience4J

## Introdução

Em sistemas distribuídos modernos, falhas são inevitáveis. Serviços externos podem ficar lentos, indisponíveis ou apresentar comportamentos inesperados. Para construir aplicações robustas, precisamos implementar padrões de resiliência que permitam que nossos sistemas continuem funcionando mesmo quando partes dele falham.

Este artigo explora um projeto prático que demonstra a implementação de vários padrões de resiliência usando Spring Boot e a biblioteca Resilience4J. Vamos entender como proteger nossas aplicações contra falhas em serviços externos e garantir uma melhor experiência para os usuários.

## O que é Resilience4J?

Resilience4J é uma biblioteca de tolerância a falhas leve e fácil de usar, projetada para aplicações Java. Inspirada no Netflix Hystrix, mas construída para Java 8 e programação funcional, ela oferece vários módulos para implementar padrões de resiliência:

- **Circuit Breaker**: Previne chamadas a serviços com falha
- **Retry**: Tenta novamente operações que falharam
- **Bulkhead**: Limita chamadas concorrentes para evitar sobrecarga
- **Rate Limiter**: Restringe a frequência de chamadas
- **Time Limiter**: Define um tempo limite para operações

## Estrutura do Projeto

Nosso projeto de exemplo é uma aplicação Spring Boot que demonstra cada um desses padrões de resiliência. Vamos explorar os principais componentes:

### Serviço Externo Simulado

Para demonstrar os padrões de resiliência, criamos um serviço que simula chamadas a um serviço externo que pode falhar aleatoriamente:

```java
@Service
public class ExternalService {
    private final Random random = new Random();

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
```

Este serviço tem 50% de chance de falhar e introduz um atraso aleatório de 0-2 segundos, simulando condições reais de rede e serviços instáveis.

### Implementação dos Padrões de Resiliência

O `CircuitBreakerService` implementa todos os padrões de resiliência usando anotações do Resilience4J:

```java
@Service
@Slf4j
public class CircuitBreakerService {
    private final ExternalService externalService;
    
    // Constantes para as instâncias
    private static final String CIRCUIT_BREAKER_INSTANCE = "externalServiceBreaker";
    private static final String RETRY_INSTANCE = "externalServiceRetry";
    private static final String BULKHEAD_INSTANCE = "externalServiceBulkhead";
    private static final String RATE_LIMITER_INSTANCE = "externalServiceRateLimiter";
    private static final String TIME_LIMITER_INSTANCE = "externalServiceTimeLimiter";

    public CircuitBreakerService(ExternalService externalService) {
        this.externalService = externalService;
    }

    @CircuitBreaker(name = CIRCUIT_BREAKER_INSTANCE, fallbackMethod = "fallbackMethod")
    public String callWithCircuitBreaker() {
        log.info("Calling external service with Circuit Breaker protection");
        return externalService.callExternalService();
    }

    @Retry(name = RETRY_INSTANCE, fallbackMethod = "fallbackMethod")
    public String callWithRetry() {
        log.info("Calling external service with Retry protection");
        return externalService.callExternalService();
    }

    @Bulkhead(name = BULKHEAD_INSTANCE, fallbackMethod = "fallbackMethod")
    public String callWithBulkhead() {
        log.info("Calling external service with Bulkhead protection");
        return externalService.callExternalService();
    }

    @RateLimiter(name = RATE_LIMITER_INSTANCE, fallbackMethod = "fallbackMethod")
    public String callWithRateLimiter() {
        log.info("Calling external service with Rate Limiter protection");
        return externalService.callExternalService();
    }

    @TimeLimiter(name = TIME_LIMITER_INSTANCE, fallbackMethod = "timeLimiterFallback")
    public CompletableFuture<String> callWithTimeLimiter() {
        log.info("Calling external service with Time Limiter protection");
        return CompletableFuture.supplyAsync(() -> externalService.callExternalService());
    }

    // Métodos de fallback
    private String fallbackMethod(Exception e) {
        log.error("Fallback method called due to: {}", e.getMessage());
        return "Fallback response: The service is currently unavailable. Please try again later.";
    }

    private CompletableFuture<String> timeLimiterFallback(Exception e) {
        log.error("Time Limiter fallback method called due to: {}", e.getMessage());
        return CompletableFuture.supplyAsync(() -> 
            "Fallback response: The service took too long to respond. Please try again later.");
    }

    private CompletableFuture<String> timeLimiterFallback(TimeoutException e) {
        log.error("Time Limiter fallback method called due to timeout: {}", e.getMessage());
        return CompletableFuture.supplyAsync(() -> 
            "Fallback response: The service request timed out. Please try again later.");
    }
}
```

### API REST para Demonstração

O `ResilienceController` expõe endpoints REST para demonstrar cada padrão de resiliência:

```java
@RestController
@RequestMapping("/api/resilience")
@Slf4j
public class ResilienceController {
    private final CircuitBreakerService circuitBreakerService;

    public ResilienceController(CircuitBreakerService circuitBreakerService) {
        this.circuitBreakerService = circuitBreakerService;
    }

    @GetMapping("/circuit-breaker")
    public String circuitBreakerDemo() {
        log.info("Circuit Breaker demo endpoint called");
        return circuitBreakerService.callWithCircuitBreaker();
    }

    @GetMapping("/retry")
    public String retryDemo() {
        log.info("Retry demo endpoint called");
        return circuitBreakerService.callWithRetry();
    }

    @GetMapping("/bulkhead")
    public String bulkheadDemo() {
        log.info("Bulkhead demo endpoint called");
        return circuitBreakerService.callWithBulkhead();
    }

    @GetMapping("/rate-limiter")
    public String rateLimiterDemo() {
        log.info("Rate Limiter demo endpoint called");
        return circuitBreakerService.callWithRateLimiter();
    }

    @GetMapping("/time-limiter")
    public CompletableFuture<String> timeLimiterDemo() {
        log.info("Time Limiter demo endpoint called");
        return circuitBreakerService.callWithTimeLimiter();
    }
}
```

### Dashboard para Monitoramento

O projeto inclui um dashboard visual para monitorar o estado dos Circuit Breakers em tempo real:

```java
@Controller
@RequestMapping("/dashboard")
public class DashboardController {
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public DashboardController(CircuitBreakerRegistry circuitBreakerRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    @GetMapping("/circuit-breakers")
    @ResponseBody
    public Map<String, Object> getCircuitBreakersState() {
        Map<String, Object> result = new HashMap<>();

        circuitBreakerRegistry.getAllCircuitBreakers().forEach(circuitBreaker -> {
            Map<String, Object> details = new HashMap<>();

            CircuitBreaker.State state = circuitBreaker.getState();
            details.put("state", state.name());

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

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("title", "Circuit Breaker Dashboard");
        return "dashboard";
    }
}
```

## Configuração dos Padrões de Resiliência

A configuração dos padrões de resiliência é feita no arquivo `application.yml`:

### Circuit Breaker

```yaml
resilience4j:
  circuitbreaker:
    instances:
      externalServiceBreaker:
        registerHealthIndicator: true
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        failureRateThreshold: 50
        waitDurationInOpenState: 10000
        permittedNumberOfCallsInHalfOpenState: 3
        automaticTransitionFromOpenToHalfOpenEnabled: true
```

- **slidingWindowSize**: Número de chamadas consideradas para calcular a taxa de falha
- **minimumNumberOfCalls**: Número mínimo de chamadas necessárias antes de calcular a taxa de falha
- **failureRateThreshold**: Porcentagem de falhas que aciona a abertura do circuito (50%)
- **waitDurationInOpenState**: Tempo que o circuito permanece aberto (10 segundos)
- **permittedNumberOfCallsInHalfOpenState**: Número de chamadas permitidas no estado meio-aberto
- **automaticTransitionFromOpenToHalfOpenEnabled**: Transição automática de aberto para meio-aberto

### Retry

```yaml
resilience4j:
  retry:
    instances:
      externalServiceRetry:
        maxAttempts: 3
        waitDuration: 1000
        enableExponentialBackoff: true
        exponentialBackoffMultiplier: 2
        retryExceptions: java.lang.RuntimeException
```

- **maxAttempts**: Número máximo de tentativas (3)
- **waitDuration**: Tempo de espera entre tentativas (1 segundo)
- **enableExponentialBackoff**: Habilita backoff exponencial
- **exponentialBackoffMultiplier**: Multiplicador para o backoff exponencial
- **retryExceptions**: Exceções que acionam a repetição

### Bulkhead

```yaml
resilience4j:
  bulkhead:
    instances:
      externalServiceBulkhead:
        maxConcurrentCalls: 10
        maxWaitDuration: 500ms
```

- **maxConcurrentCalls**: Número máximo de chamadas concorrentes (10)
- **maxWaitDuration**: Tempo máximo de espera para entrar no bulkhead (500ms)

### Rate Limiter

```yaml
resilience4j:
  ratelimiter:
    instances:
      externalServiceRateLimiter:
        limitForPeriod: 5
        limitRefreshPeriod: 10s
        timeoutDuration: 0
```

- **limitForPeriod**: Número máximo de chamadas permitidas no período (5)
- **limitRefreshPeriod**: Período após o qual o limite é redefinido (10 segundos)
- **timeoutDuration**: Tempo máximo de espera para adquirir uma permissão (0 = não espera)

### Time Limiter

```yaml
resilience4j:
  timelimiter:
    instances:
      externalServiceTimeLimiter:
        timeoutDuration: 1s
        cancelRunningFuture: true
```

- **timeoutDuration**: Tempo máximo permitido para a execução (1 segundo)
- **cancelRunningFuture**: Cancela a execução se o tempo limite for excedido

## Como Testar os Padrões de Resiliência

### Circuit Breaker

1. Acesse `/api/resilience/circuit-breaker` repetidamente
2. Após várias falhas (cerca de 5), o circuito abrirá
3. Chamadas subsequentes retornarão imediatamente a resposta de fallback
4. Após 10 segundos, o circuito transitará para meio-aberto
5. Se as chamadas forem bem-sucedidas no estado meio-aberto, o circuito fechará

### Retry

1. Acesse `/api/resilience/retry`
2. Se a chamada falhar, ela será repetida até 3 vezes
3. Cada nova tentativa esperará mais tempo devido ao backoff exponencial

### Bulkhead

1. Faça múltiplas chamadas concorrentes para `/api/resilience/bulkhead`
2. Após 10 chamadas concorrentes, chamadas adicionais serão rejeitadas

### Rate Limiter

1. Acesse `/api/resilience/rate-limiter` repetidamente
2. Após 5 chamadas dentro de 10 segundos, chamadas adicionais serão rejeitadas
3. Após 10 segundos, o limite de taxa será redefinido

### Time Limiter

1. Acesse `/api/resilience/time-limiter`
2. Se a chamada levar mais de 1 segundo, ela será cancelada e retornará a resposta de fallback

## Dashboard de Monitoramento

O projeto inclui um dashboard visual para monitorar o estado dos Circuit Breakers em tempo real. Acesse `/dashboard` para visualizar:

- Estado atual de cada Circuit Breaker (FECHADO, ABERTO ou MEIO-ABERTO) com codificação de cores
- Taxa de falha
- Taxa de chamadas lentas
- Número de chamadas bem-sucedidas
- Número de chamadas com falha
- Número de chamadas lentas
- Número de chamadas não permitidas

O dashboard atualiza automaticamente a cada 5 segundos, ou você pode atualizar manualmente os dados usando o botão "Refresh Data".

## Conclusão

Implementar padrões de resiliência é essencial para construir sistemas distribuídos robustos. O Resilience4J, combinado com o Spring Boot, oferece uma maneira elegante e eficaz de proteger nossas aplicações contra falhas em serviços externos.

Neste projeto, demonstramos cinco padrões de resiliência importantes:

1. **Circuit Breaker**: Previne chamadas a serviços com falha e fornece alternativas
2. **Retry**: Automaticamente tenta novamente chamadas que falharam
3. **Bulkhead**: Limita chamadas concorrentes para prevenir esgotamento de recursos
4. **Rate Limiter**: Restringe a frequência de chamadas
5. **Time Limiter**: Define um tempo limite para chamadas

Ao implementar esses padrões, podemos criar aplicações mais resilientes que continuam funcionando mesmo quando partes do sistema falham, proporcionando uma melhor experiência para os usuários.

## Como Executar o Projeto

Para executar o projeto, você precisa ter o Java 17 e o Gradle instalados. Clone o repositório e execute:

```bash
./gradlew bootRun
```

A aplicação estará disponível em `http://localhost:8080`.

## Recursos Adicionais

- [Documentação do Resilience4J](https://resilience4j.readme.io/)
- [Guia do Spring Boot](https://spring.io/projects/spring-boot)
- [Padrão Circuit Breaker por Martin Fowler](https://martinfowler.com/bliki/CircuitBreaker.html)