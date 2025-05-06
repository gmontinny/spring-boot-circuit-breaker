# Configuração do Kubernetes para Spring Boot Circuit Breaker

Este diretório contém os arquivos de configuração do Kubernetes para implantar a aplicação Spring Boot Circuit Breaker em um cluster Kubernetes.

## Pré-requisitos

- Docker instalado
- Cluster Kubernetes (local ou remoto)
- kubectl configurado para conectar ao seu cluster
- (Opcional) Controlador de Ingress instalado no seu cluster

## Arquivos

- `deployment.yaml` - Define a implantação da aplicação
- `service.yaml` - Define os serviços para expor a aplicação
- `configmap.yaml` - Contém a configuração da aplicação
- `ingress.yaml` - Define o ingress para acesso externo

## Passos para Implantação

### 1. Construir a Imagem Docker

A partir do diretório raiz do projeto, construa a imagem Docker:

```bash
docker build -t spring-boot-circuit-breaker:latest .
```

### 2. (Opcional) Enviar a Imagem para um Registro

Se você estiver usando um cluster Kubernetes remoto, envie a imagem para um registro de contêineres:

```bash
docker tag spring-boot-circuit-breaker:latest seu-registro/spring-boot-circuit-breaker:latest
docker push seu-registro/spring-boot-circuit-breaker:latest
```

Em seguida, atualize o campo `image` no arquivo `deployment.yaml` para apontar para o seu registro.

### 3. Aplicar os Recursos do Kubernetes

#### Opção 1: Aplicar Recursos Individualmente

Aplique o ConfigMap primeiro:

```bash
kubectl apply -f kubernetes/configmap.yaml
```

Em seguida, aplique o deployment:

```bash
kubectl apply -f kubernetes/deployment.yaml
```

Aplique os serviços:

```bash
kubectl apply -f kubernetes/service.yaml
```

Finalmente, se você tiver um controlador de ingress instalado, aplique o ingress:

```bash
kubectl apply -f kubernetes/ingress.yaml
```

#### Opção 2: Aplicar Todos os Recursos de Uma Vez Usando Kustomize

Um arquivo `kustomization.yaml` é fornecido para facilitar a aplicação de todos os recursos de uma vez:

```bash
kubectl apply -k kubernetes/
```

Isso aplicará todos os recursos na ordem correta e adicionará rótulos comuns a todos os recursos.

### 4. Acessar a Aplicação

#### Usando NodePort

A aplicação é exposta na porta 30080 de qualquer nó no seu cluster:

```
http://<ip-do-nó>:30080
```

#### Usando Ingress

Se você aplicou o ingress e configurou o DNS para apontar para o seu controlador de ingress, você pode acessar a aplicação em:

```
http://circuit-breaker.example.com
```

Substitua `circuit-breaker.example.com` pelo seu domínio real.

## Monitoramento

A aplicação expõe endpoints do Actuator para monitoramento. Você pode acessá-los em:

- Saúde: `/actuator/health`
- Eventos do Circuit Breaker: `/actuator/circuitbreakerevents`
- Eventos de Retry: `/actuator/retryevents`
- Eventos de Bulkhead: `/actuator/bulkheadevents`
- Eventos de Rate Limiter: `/actuator/ratelimiterevents`

A aplicação também fornece um dashboard em `/dashboard` para monitorar o estado dos Circuit Breakers em tempo real.

## Escalabilidade

Você pode escalar a aplicação alterando o campo `replicas` no arquivo `deployment.yaml` ou usando o comando `kubectl scale`:

```bash
kubectl scale deployment spring-boot-circuit-breaker --replicas=3
```

## Solução de Problemas

Se você encontrar problemas, verifique os logs dos pods:

```bash
kubectl logs -l app=spring-boot-circuit-breaker
```

Você também pode verificar o status dos pods:

```bash
kubectl get pods -l app=spring-boot-circuit-breaker
```

Para informações mais detalhadas sobre um pod:

```bash
kubectl describe pod <nome-do-pod>
```
