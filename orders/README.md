# Getting Started

## Flyway

To recreate items:

```postgresql
DELETE FROM flyway_schema_history WHERE version = '20260924';
```

### Query example

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
