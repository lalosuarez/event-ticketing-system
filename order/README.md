# Getting Started

## Flyway

To recreate items:

```postgresql
DELETE FROM flyway_schema_history WHERE version = '20260924';
```

### Get Orders

http://localhost:8081/graphiql

Pass `X-User-Timezone` header to get datetime values in the specified timezone.

Uses cursor based pagination. To traverse through the pages you need to make the first call like the following

```graphql
query GetOrders {
  orders(first: 10) {
    edges {
      node {
        id
        userId
        status
        ticket {
          id
          price
          title
        }
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
query GetOrders {
  orders(first: 10, after: "T18z") {
    edges {
      node {
        id
        userId
        status
        ticket {
          id
          price
          title
        }
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
query GetOrderById {
  order(id: "01a14195-780f-7477-af65-b85dee948e56") {
    id
    userId
    status
    ticket {
      id
      price
      title
    }
  }
}
```

### Create Order

```graphql
mutation CreateOrder {
  create(createOrderRequest: { ticketId: "01a1417e-15e0-7d94-8b43-4395a1f3bccb" }) {
    id
    userId
    status
    expiresAt
    ticket {
      price
      title
    }
  }
}
```

### Cancel Order

```graphql
mutation CancelOrder {
  cancel(cancelOrderRequest: { orderId: "01a14189-6b63-7c0b-8467-238baffa7fc7" }) {
    id
    userId
    status
    ticket {
      price
      title
    }
  }
}
```

### CURL example

```shell
curl --url 'http://localhost:8081/graphql' \
  -H 'accept: application/json, multipart/mixed' \
  -H 'content-type: application/json' \
  -H 'X-User-Timezone: America/Chicago' \
  -H 'authorization: Bearer $ACCESS_TOKEN' \
  --data-raw '{"operationName":"GetOrders","query":"mutation CreateOrder {\n  create(\n    createOrderRequest: {ticketId: \"01a144c7-3fac-7bd1-b9d2-13c936f298b0\"}\n  ) {\n    id\n    userId\n    status\n    expiresAt\n    ticket {\n      price\n      title\n    }\n  }\n}\n\nmutation CancelOrder {\n  cancel(\n    cancelOrderRequest: {orderId: \"01a14189-6b63-7c0b-8467-238baffa7fc7\"}\n  ) {\n    id\n    userId\n    status\n    ticket {\n      price\n      title\n    }\n  }\n}\n\nquery GetOrders {\n  orders(first: 10) {\n    edges {\n      node {\n        id\n        userId\n        status\n        ticket {\n          id\n          price\n          title\n        }\n        dateTimeUtc\n        dateTimeLocal\n      }\n    }\n    pageInfo {\n      hasNextPage\n      hasPreviousPage\n      startCursor\n      endCursor\n    }\n  }\n}\n\nquery GetOrderById {\n  order(id: \"01a14197-4a7a-7687-aaef-4d608cf24711\") {\n    id\n    userId\n    status\n    ticket {\n      id\n      price\n      title\n    }\n    dateTimeUtc\n    dateTimeLocal\n  }\n}\n"}'
```
