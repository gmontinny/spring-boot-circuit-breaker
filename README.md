# Exemplo de Circuit Breaker com Spring Boot

Este projeto demonstra o uso do padrão Circuit Breaker e outros padrões de resiliência usando Resilience4J em uma aplicação Spring Boot.

## Visão Geral

O padrão Circuit Breaker é um padrão de design usado no desenvolvimento de software moderno para prevenir falhas em cascata em sistemas distribuídos. Este projeto demonstra não apenas o padrão Circuit Breaker, mas também outros padrões de resiliência fornecidos pelo Resilience4J:

1. **Circuit Breaker** - Previne chamadas a serviços com falha e fornece alternativas
2. **Retry** - Automaticamente tenta novamente chamadas que falharam
3. **Bulkhead** - Limita chamadas concorrentes para prevenir esgotamento de recursos
4. **Rate Limiter** - Restringe a frequência de chamadas
5. **Time Limiter** - Define um tempo limite para chamadas

## Estrutura do Projeto

- `ExternalService` - Simula um serviço externo que pode falhar aleatoriamente
- `CircuitBreakerService` - Implementa todos os padrões de resiliência para proteger chamadas ao serviço externo
- `ResilienceController` - Expõe endpoints REST para demonstrar cada padrão de resiliência

## Endpoints

A aplicação expõe os seguintes endpoints:

- **Circuit Breaker**: `GET /api/resilience/circuit-breaker`
  - Demonstra como o circuito abre após múltiplas falhas e usa alternativas
  - Após o tempo de espera, ele transita para meio-aberto e então fechado se for bem-sucedido

- **Retry**: `GET /api/resilience/retry`
  - Demonstra a repetição automática de chamadas que falharam
  - Usa backoff exponencial entre as tentativas

- **Bulkhead**: `GET /api/resilience/bulkhead`
  - Demonstra a limitação de chamadas concorrentes ao serviço
  - Previne esgotamento de recursos sob alta carga

- **Rate Limiter**: `GET /api/resilience/rate-limiter`
  - Demonstra a limitação da taxa de chamadas ao serviço
  - Previne sobrecarga do serviço

- **Time Limiter**: `GET /api/resilience/time-limiter`
  - Demonstra a definição de um tempo limite para chamadas ao serviço
  - Previne que chamadas de longa duração bloqueiem recursos

## Configuração

Os padrões de resiliência da aplicação são configurados no `application.yml`:

### Configuração do Circuit Breaker
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

### Configuração de Retry
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

### Configuração de Bulkhead
```yaml
resilience4j:
  bulkhead:
    instances:
      externalServiceBulkhead:
        maxConcurrentCalls: 10
        maxWaitDuration: 500ms
```

### Configuração de Rate Limiter
```yaml
resilience4j:
  ratelimiter:
    instances:
      externalServiceRateLimiter:
        limitForPeriod: 5
        limitRefreshPeriod: 10s
        timeoutDuration: 0
```

### Configuração de Time Limiter
```yaml
resilience4j:
  timelimiter:
    instances:
      externalServiceTimeLimiter:
        timeoutDuration: 1s
        cancelRunningFuture: true
```

## Como Testar

1. Inicie a aplicação
2. Use uma ferramenta como Postman ou curl para chamar os endpoints
3. Observe o comportamento de cada padrão de resiliência

### Testando o Circuit Breaker

1. Chame `/api/resilience/circuit-breaker` repetidamente
2. Após várias falhas (cerca de 5), o circuito abrirá
3. Chamadas subsequentes retornarão imediatamente a resposta alternativa
4. Após 10 segundos, o circuito transitará para meio-aberto
5. Se as chamadas forem bem-sucedidas no estado meio-aberto, o circuito fechará

### Testando o Retry

1. Chame `/api/resilience/retry`
2. Se a chamada falhar, ela será repetida até 3 vezes
3. Cada nova tentativa esperará mais tempo devido ao backoff exponencial

### Testando o Bulkhead

1. Faça múltiplas chamadas concorrentes para `/api/resilience/bulkhead`
2. Após 10 chamadas concorrentes, chamadas adicionais serão rejeitadas

### Testando o Rate Limiter

1. Chame `/api/resilience/rate-limiter` repetidamente
2. Após 5 chamadas dentro de 10 segundos, chamadas adicionais serão rejeitadas
3. Após 10 segundos, o limite de taxa será redefinido

### Testando o Time Limiter

1. Chame `/api/resilience/time-limiter`
2. Se a chamada levar mais de 1 segundo, ela será cancelada e retornará a alternativa

## Monitoramento

### Endpoints do Actuator

A aplicação expõe endpoints do Actuator para monitoramento:

- Saúde: `/actuator/health`
- Eventos do Circuit Breaker: `/actuator/circuitbreakerevents`
- Eventos de Retry: `/actuator/retryevents`
- Eventos de Bulkhead: `/actuator/bulkheadevents`
- Eventos de Rate Limiter: `/actuator/ratelimiterevents`

### Dashboard

A aplicação também fornece um dashboard visual para monitorar o estado dos Circuit Breakers em tempo real:

- Dashboard: `/dashboard`

O dashboard exibe as seguintes informações para cada Circuit Breaker:
- Estado atual (FECHADO, ABERTO ou MEIO-ABERTO) com codificação de cores
- Taxa de falha
- Taxa de chamadas lentas
- Número de chamadas bem-sucedidas
- Número de chamadas com falha
- Número de chamadas lentas
- Número de chamadas não permitidas

O dashboard atualiza automaticamente a cada 5 segundos, ou você pode atualizar manualmente os dados usando o botão "Atualizar Dados".

## Executando a Aplicação

```bash
./gradlew bootRun
```

## Dependências

- Spring Boot 3.4.5
- Spring Cloud Circuit Breaker
- Resilience4J
- Lombok