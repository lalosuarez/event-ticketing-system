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
  stripePayment(
    stripePaymentRequest: { orderId: "01a1224c-289f-7206-88bf-6d1c2a9edc3b", token: "tok_1234" }
  ) {
    status
  }
}
```
