# event-ticketing-system

Microservices architecture with GraphQl and Saga pattern using choreography for comunication between services.
Security implemented with keycloak.

Overall Architecture:

```text
[ UI Client ] 
    │
    ▼ (HTTPS, OAuth2 Token)
[ Spring Cloud Gateway ]  <──► [ Keycloak ] (Token Validation / Token Exchange)
    │
    ▼ (HTTP, Validated Context/Headers)
[ Apollo Router / Gateway ]
    │
    ├──► [ Ticket (GraphQL Subgraph) ]
    ├──► [ Order (GraphQL Subgraph) ]
    └──► [ Payment (GraphQL Subgraph) ]
```

## Install:
- Docker/Docker Desktop
- Kubernetes (If you have Docker Desktop you can install it from there)
- Skaffold

## Ports reference

- 54321 - Postgres
- 9091 - keycloak
- 9092 - kafka
- 8081 - orders
- 8082 - ticket
- 8083 - payment
- 8084 - gateway

## Create .env file

Create .env file under `infra` directory with the following values:

```text
DB_USERNAME
DB_PASSWORD
RABBITMQ_USERNAME
RABBITMQ_PASSWORD
KEYCLOAK_USERNAME
KEYCLOAK_PASSWORD
GATEWAY_SECRET
```

## Start Docker for local env

Set permissions for script that creates the different databases:

```shell
chmod +x infra/init-databases.sh
```

Start:

```shell
docker compose -f infra/compose.yaml --env-file infra/.env up -d
```

Stop:

```shell
docker compose -f infra/compose.yaml --env-file infra/.env down

# Or to delete volumes
docker compose -f infra/compose.yaml --env-file infra/.env down -v

# Or stop just one service
docker compose -f infra/compose.yaml --env-file infra/.env stop kafka
```

```shell
docker exec -i kafka /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic ticket.created \
  --from-beginning \
  --property print.timestamp=true \
  --property print.key=true \
  --property print.partition=true \
  --property print.offset=true

printf '__TypeId__:com.eventtickets.ticket.TicketCreatedEvent\t{"id":"01a144c7-2d68-7c26-9ae0-0a7a5ecfb1c4","title":"Concert 1","price":100.00,"userId":"d50baa03-4468-4676-a797-6f61e0524e99","version":0}\n' | \
  docker exec -i kafka /opt/kafka/bin/kafka-console-producer.sh \
  --bootstrap-server localhost:9092 \
  --topic ticket.created \
  --reader-property parse.headers=true

docker exec -i kafka /opt/kafka/bin/kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --group order \
  --topic ticket.created \
  --reset-offsets \
  --to-offset 7 \
  --execute
```

You can manually start each service from terminal or IDE, or run skaffold.

## Skaffold

```shell
skaffold dev --port-forward
```

## Memory
Set VM options:

```shell
export JAVA_TOOL_OPTIONS="-Xms128m -Xmx512m -XX:+UseSerialGC"
```

Keycloak: http://localhost:9091

Make sure to create users for groups buyer, seller, read-only:
- user1 - buyer
- user2 - seller
- user3 - buyer & seller
- user4 - read-only
- user5 - no groups assigned
- user6 - buyer & seller

## Securtity

Get token by calling Keycloak:

### Public Client for gateway, ticket, order, and payment:

```shell
ACCESS_TOKEN=$(curl --location 'http://localhost:9091/realms/event-ticketing/protocol/openid-connect/token' \
  --header 'Content-Type: application/x-www-form-urlencoded' \
  --data-urlencode 'grant_type=password' \
  --data-urlencode 'username=user1' \
  --data-urlencode 'password=secret' \
  --data-urlencode 'scope=openid' \
  --data-urlencode 'client_id=spa-web-app' | jq -r '.access_token')
```

### Confidential Client for ticket:

```shell
ACCESS_TOKEN=$(curl --location 'http://localhost:9091/realms/event-ticketing/protocol/openid-connect/token' \
  --header 'Content-Type: application/x-www-form-urlencoded' \
  --data-urlencode 'grant_type=password' \
  --data-urlencode 'username=user1' \
  --data-urlencode 'password=secret' \
  --data-urlencode 'scope=openid' \
  --data-urlencode 'client_id=ticket-service' \
  --data-urlencode 'client_secret=aUU4cHRrMSw839jQ4FmJOgNaV26U6S4l' | jq -r '.access_token')
```

### Confidential Client for order:

```shell
ACCESS_TOKEN=$(curl --location 'http://localhost:9091/realms/event-ticketing/protocol/openid-connect/token' \
  --header 'Content-Type: application/x-www-form-urlencoded' \
  --data-urlencode 'grant_type=password' \
  --data-urlencode 'username=user1' \
  --data-urlencode 'password=secret' \
  --data-urlencode 'scope=openid' \
  --data-urlencode 'client_id=order-service' \
  --data-urlencode 'client_secret=7Fz9HTQo8zyyX2rkV442Geg1anASe0BU' | jq -r '.access_token')
```

### Confidential Client for payment:

```shell
ACCESS_TOKEN=$(curl --location 'http://localhost:9091/realms/event-ticketing/protocol/openid-connect/token' \
  --header 'Content-Type: application/x-www-form-urlencoded' \
  --data-urlencode 'grant_type=password' \
  --data-urlencode 'username=user1' \
  --data-urlencode 'password=secret' \
  --data-urlencode 'scope=openid' \
  --data-urlencode 'client_id=payment-service' \
  --data-urlencode 'client_secret=QOuxZme5d9pjrlSU2uBVK05kHAQac6UX' | jq -r '.access_token')
```

### Confidential Client for gateway:

```shell
ACCESS_TOKEN=$(curl --location 'http://localhost:9091/realms/event-ticketing/protocol/openid-connect/token' \
  --header 'Content-Type: application/x-www-form-urlencoded' \
  --data-urlencode 'grant_type=password' \
  --data-urlencode 'username=user1' \
  --data-urlencode 'password=secret' \
  --data-urlencode 'scope=openid' \
  --data-urlencode 'client_id=gateway-service' \
  --data-urlencode 'client_secret=Pd8G4vR9yOVqovsEEzOndE5Llsw9HPcW' | jq -r '.access_token')
```
