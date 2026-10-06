# Getting Started

## Flyway

To recreate items:

```postgresql
DELETE FROM flyway_schema_history WHERE version = '20260924';
```

### Create Ticket

http://localhost:8082/graphiql

```graphql
mutation MyMutation {
    create(
        createTicketRequest: {
            title: "Concert 1"
            price: "100.00"
            userId: "userId-2"
        }
    ) {
        id
        price
        title
        userId
    }
}
```

### Update Ticket

```graphql
mutation MyMutation {
    update(
        updateTicketRequest: {
            title: "Concert 1"
            price: "199.99"
            id: "01a111db-5d9b-7d3c-8255-67012132c7b6"
            userId: "userId-1"
        }
    ) {
        id
        price
        title
        userId
    }
}
```
