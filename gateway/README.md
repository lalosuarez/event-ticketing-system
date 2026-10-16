# Read Me First

## CURL examples

### Ticket

```shell
curl -v -s -X POST 'http://localhost:8084/ticket' \
  -H 'Accept: application/json, multipart/mixed' \
  -H 'Content-Type: application/json' \
  -H 'X-User-Timezone: America/Chicago' \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  --data-raw '{"operationName":"GetTicketsForPurchase","query":"mutation CreateTicket {\n  create(createTicketRequest: { title: \"Concert 3\", price: \"100.00\" }) {\n    id\n    price\n    title\n    userId\n  }\n}\n\nmutation UpdateTicket {\n  update(\n    updateTicketRequest: {\n      id: \"01a144c7-3fac-7bd1-b9d2-13c936f298b0\"\n      title: \"Concert 2\"\n      price: \"150.00\"\n    }\n  ) {\n    id\n    price\n    title\n    userId\n  }\n}\n\nquery GetTicketById {\n  ticket(id: \"01a14498-35e8-77c5-be28-30731ecb1c0b\") {\n    price\n    title\n    userId\n    orderId\n  }\n}\n\nquery GetTicketsForPurchase {\n  ticketsForPurchase(first: 10) {\n    edges {\n      cursor\n      node {\n        id\n        title\n        price\n        userId\n      }\n    }\n    pageInfo {\n      endCursor\n      hasNextPage\n      hasPreviousPage\n      startCursor\n    }\n  }\n}\n\nquery GetTickets {\n  tickets(first: 10) {\n    edges {\n      cursor\n      node {\n        id\n        title\n        price\n        userId\n        orderId\n      }\n    }\n    pageInfo {\n      endCursor\n      hasNextPage\n      hasPreviousPage\n      startCursor\n    }\n  }\n}"}' \
  | jq
```

### Order

```shell
curl -v -s -X POST 'http://localhost:8084/order' \
  -H 'Accept: application/json, multipart/mixed' \
  -H 'Content-Type: application/json' \
  -H 'X-User-Timezone: America/Chicago' \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  --data-raw '{"operationName":"GetOrders","query":"mutation CreateOrder {\n  create(\n    createOrderRequest: {ticketId: \"01a144c7-3fac-7bd1-b9d2-13c936f298b0\"}\n  ) {\n    id\n    userId\n    status\n    expiresAt\n    ticket {\n      price\n      title\n    }\n  }\n}\n\nmutation CancelOrder {\n  cancel(\n    cancelOrderRequest: {orderId: \"01a14189-6b63-7c0b-8467-238baffa7fc7\"}\n  ) {\n    id\n    userId\n    status\n    ticket {\n      price\n      title\n    }\n  }\n}\n\nquery GetOrders {\n  orders(first: 10) {\n    edges {\n      node {\n        id\n        userId\n        status\n        ticket {\n          id\n          price\n          title\n        }\n        dateTimeUtc\n        dateTimeLocal\n      }\n    }\n    pageInfo {\n      hasNextPage\n      hasPreviousPage\n      startCursor\n      endCursor\n    }\n  }\n}\n\nquery GetOrderById {\n  order(id: \"01a14197-4a7a-7687-aaef-4d608cf24711\") {\n    id\n    userId\n    status\n    ticket {\n      id\n      price\n      title\n    }\n    dateTimeUtc\n    dateTimeLocal\n  }\n}\n"}' \
  | jq
```

### Payment

```shell
curl -v -s -X POST 'http://localhost:8084/payment' \
  -H 'Accept: application/json, multipart/mixed' \
  -H 'Content-Type: application/json' \
  -H 'X-User-Timezone: America/Chicago' \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  --data-raw '{"operationName":"StripePayment","query":"mutation StripePayment {\n  stripePayment(\n    stripePaymentRequest: {\n      orderId: \"01a14195-780f-7477-af65-b85dee948e56\"\n      token: \"tok_1234\"\n    }\n  ) {\n    status\n  }\n}"}' \
  | jq
```



curl --url 'http://localhost:8083/graphql' \
-H 'Accept-Language: en-US,en;q=0.9' \
-H 'Cache-Control: no-cache' \
-H 'Connection: keep-alive' \
-b 'Idea-ed496aa5=ba10d6ff-d6b8-4a6a-9708-f2bc7b8b08cb' \
-H 'Origin: http://localhost:8083' \
-H 'Pragma: no-cache' \
-H 'Referer: http://localhost:8083/graphiql?path=/graphql' \
-H 'Sec-Fetch-Dest: empty' \
-H 'Sec-Fetch-Mode: cors' \
-H 'Sec-Fetch-Site: same-origin' \
-H 'User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/155.0.0.0 Safari/537.36' \
-H 'accept: application/json, multipart/mixed' \
-H 'authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJpTFVublVsVFJZaHpNZE5jS0ZBdXhrME9lcGdURVpBUks3aEJxQ0U2RDZJIn0.eyJleHAiOjE3OTIxNzcwMTMsImlhdCI6MTc5MjE3NjExMywianRpIjoiZGI5MzVlY2YtZjg3Yy00YzgxLTk3ZDYtY2NjZWUzOTFkODgyIiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo5MDkxL3JlYWxtcy9ldmVudC10aWNrZXRpbmciLCJhdWQiOlsiaHR0cDovL3RpY2tldCIsImh0dHA6Ly9wYXltZW50IiwiaHR0cDovL2dhdGV3YXkiLCJodHRwOi8vb3JkZXIiLCJwYXltZW50LXNlcnZpY2UiLCJnYXRld2F5LXNlcnZpY2UiLCJvcmRlci1zZXJ2aWNlIiwidGlja2V0LXNlcnZpY2UiXSwic3ViIjoiMjAzNTg2N2QtYzExMi00N2RiLTgwNjktZTgwNDE0NDlmMjA0IiwidHlwIjoiQmVhcmVyIiwiYXpwIjoic3BhLXdlYi1hcHAiLCJzZXNzaW9uX3N0YXRlIjoiYWU3NGJmMmItMjFmOC00ZmU5LThkZTUtMzRjYjYyY2JiNDMyIiwiYWNyIjoiMSIsImFsbG93ZWQtb3JpZ2lucyI6WyJodHRwOi8vbG9jYWxob3N0OjgwODMiXSwicmVzb3VyY2VfYWNjZXNzIjp7InBheW1lbnQtc2VydmljZSI6eyJyb2xlcyI6WyJwYXltZW50X293bmVyIl19LCJnYXRld2F5LXNlcnZpY2UiOnsicm9sZXMiOlsiZ2F0ZXdheV9yZWFkX29ubHkiXX0sIm9yZGVyLXNlcnZpY2UiOnsicm9sZXMiOlsib3JkZXJfb3duZXIiXX0sInRpY2tldC1zZXJ2aWNlIjp7InJvbGVzIjpbInRpY2tldF9yZWFkX29ubHkiXX19LCJzY29wZSI6Im9wZW5pZCBlbWFpbCBvcmRlcjp3cml0ZSBnYXRld2F5OnJlYWQgdGlja2V0OndyaXRlIHBheW1lbnQ6d3JpdGUgcHJvZmlsZSIsInNpZCI6ImFlNzRiZjJiLTIxZjgtNGZlOS04ZGU1LTM0Y2I2MmNiYjQzMiIsImVtYWlsX3ZlcmlmaWVkIjp0cnVlLCJuYW1lIjoiVXNlcjEgQnV5ZXIiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJ1c2VyMSIsImdpdmVuX25hbWUiOiJVc2VyMSIsImZhbWlseV9uYW1lIjoiQnV5ZXIiLCJlbWFpbCI6InVzZXIxQHRlc3QuY29tIn0.oPNVkKUqrWxN8yeM14ChZUeFeU6xSZN8vsbFR6CCJtycLiV5wl8iwGS2CJVQ7VYZb0u1kxBmlcO7QgLPgld4rCRD_2UYBlXajkxtsbycMHCXIVttppaEtF4OiDM_4BhFayt7SqlCTY72AAB6y9GAYOFTZXkoQ4IpzqNqXWXV84C-Dmw2DpCyusK3RCG9K5-iqrfhJfvZv-OOTnc1JBI848YNAj2uSM9ndz5QnrGJ8LZS5vcO0t5EVWXwO0E03n5NRMHhR9-5-epabNZN-kVT6BZ9Ci9RQTaCh0sK57UXla6Ynkdv12xz2aPallX8m466P6t54fHL4IiePNdUMJTzTA' \
-H 'content-type: application/json' \
-H 'sec-ch-ua: "Google Chrome";v="155", "Chromium";v="155", "Not(A:Brand";v="24"' \
-H 'sec-ch-ua-mobile: ?0' \
-H 'sec-ch-ua-platform: "macOS"' \
--data-raw '{"operationName":"StripePayment","query":"mutation StripePayment {\n  stripePayment(\n    stripePaymentRequest: {\n      orderId: \"01a14195-780f-7477-af65-b85dee948e56\"\n      token: \"tok_1234\"\n    }\n  ) {\n    status\n  }\n}"}'