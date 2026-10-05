# event-ticketing-system

## Install:
- Docker/Docker Desktop
- Kubernetes (If you have Docker Desktop you can install it from there)
- Skaffold

## Ports reference

- 54321 - Postgres
- 8081 - orders

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

Start:

```shell
docker compose -f infra/compose.yaml --env-file infra/.env up -d
```

Stop:

```shell
docker compose -f infra/compose.yaml --env-file infra/.env down
```

## Skaffold

```shell
skaffold dev --port-forward
```
