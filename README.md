# event-ticketing-system

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
```

You can manually start each service from terminal or IDE, or run skaffold.

## Skaffold

```shell
skaffold dev --port-forward
```

Keycloak: http://localhost:9091

## Securtity

Get token by calling Keycloak:


Public Client:

```shell
ACCESS_TOKEN=$(curl --location 'http://localhost:9091/realms/event-ticketing/protocol/openid-connect/token' \
  --header 'Content-Type: application/x-www-form-urlencoded' \
  --data-urlencode 'grant_type=password' \
  --data-urlencode 'username=user1' \
  --data-urlencode 'password=secret' \
  --data-urlencode 'scope=openid ticket:readonly' \
  --data-urlencode 'client_id=spa-web-app' | jq -r '.access_token')
```

Confidential Client 1:

```shell
ACCESS_TOKEN=$(curl --location 'http://localhost:9091/realms/event-ticketing/protocol/openid-connect/token' \
  --header 'Content-Type: application/x-www-form-urlencoded' \
  --data-urlencode 'grant_type=password' \
  --data-urlencode 'username=user1' \
  --data-urlencode 'password=secret' \
  --data-urlencode 'scope=openid ticket:write' \
  --data-urlencode 'client_id=ticket-service' \
  --data-urlencode 'client_secret=aUU4cHRrMSw839jQ4FmJOgNaV26U6S4l' | jq -r '.access_token')
```
