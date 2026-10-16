# Getting Started

## Flyway

To recreate items:

```postgresql
DELETE FROM flyway_schema_history WHERE version = '20260924';
```

### Create Ticket

http://localhost:8082/graphiql

```graphql
mutation CreateTicket {
  create(createTicketRequest: {title: "Concert 1", price: "100.00"}) {
    id
    price
    title
    userId
  }
}
```

### Update Ticket

```graphql
mutation UpdateTicket {
  update(
    updateTicketRequest: {
      id: "01a1417e-15e0-7d94-8b43-4395a1f3bccb"
      title: "Concert 2"
      price: "150.00"
    }
  ) {
    id
    price
    title
    userId
  }
}
```

### Get Ticket by id

```graphql
query GetTicketById {
  ticket(id: "01a1417e-15e0-7d94-8b43-4395a1f3bccb") {
    price
    title
    userId
  }
}
```

### Get Tickets for purchase

```graphql
query GetTicketsForPurchase {
  ticketsForPurchase(first: 10) {
    edges {
      cursor
      node {
        id
        title
        price
        userId
      }
    }
    pageInfo {
      endCursor
      hasNextPage
      hasPreviousPage
      startCursor
    }
  }
}
```

### Get Tickets

```graphql
query GetTickets {
  tickets(first: 10) {
    edges {
      cursor
      node {
        id
        title
        price
      }
    }
    pageInfo {
      endCursor
      hasNextPage
      hasPreviousPage
      startCursor
    }
  }
}
```