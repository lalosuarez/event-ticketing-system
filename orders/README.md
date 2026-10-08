# Getting Started

## Flyway

To recreate items:

```postgresql
DELETE FROM flyway_schema_history WHERE version = '20260924';
```

### Get Orders

http://localhost:8081/graphiql

Uses cursor based pagination. To traverse through the pages you need to make the first call like the following

```graphql
query MyQuery {
    orders(first: 10) {
        edges {
            node {
                id
                userId
                ticketId
            }
        }
        pageInfo {
            hasNextPage
            hasPreviousPage
            startCursor
            endCursor
        }
    }
}
```

Then grab `endCursor` from the response and pass the value in the `after` argument

```graphql
query MyQuery {
    orders(first: 10, after: "T18z") {
        edges {
            node {
                id
                userId
                ticketId
            }
        }
        pageInfo {
            hasNextPage
            hasPreviousPage
            startCursor
            endCursor
        }
    }
}

```

### Get Order by id

```graphql
query MyQuery {
    order(id: "01a10d6f-0b16-7579-8450-b87b73824335") {
        ticketId
        userId
        expiresAt
        status
    }
}
```

### Create Order

```graphql
mutation MyMutation {
  create(createOrderRequest: { userId: "usr1", ticketId: "01a0f256-bf4e-7043-b9bc-7f45f9f8e79e" }) {
    id
    status
    expiresAt
  }
}
```

### Cancel Order

```graphql
mutation MyMutation {
  cancel(cancelOrderRequest: { userId: "user99", orderId: "" }) {
    id
    status
    expiresAt
  }
}
```

curl 'http://localhost:8081/graphql' \
-H 'accept: application/json, multipart/mixed' \
-H 'content-type: application/json' \
--data-raw '{"query":"mutation MyMutation {\n  create(createOrderRequest: { userId: \"userf256-bf4e-7043-b9bc-7f45f9f8e79e\" }) {\n    id\n    status\n    expiresAt\n  }\n}","operationName":"MyMutation"}'
