# Getting Started

## Flyway

To recreate items:

```postgresql
DELETE FROM flyway_schema_history WHERE version = '20260924';
```

### Create Payment

http://localhost:8083/graphiql

```graphql
mutation PaymentMutation {
  payment(paymentRequest: { userId: "1", token: "01a10d6f-0b16-7579-8450-b87b73824335" }) {
    status
  }
}
```
