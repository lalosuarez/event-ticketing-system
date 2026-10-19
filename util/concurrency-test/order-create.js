const axios = require('axios');

const USERS = [
  {
    user: 'user1',
    token: 'eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJpTFVublVsVFJZaHpNZE5jS0ZBdXhrME9lcGdURVpBUks3aEJxQ0U2RDZJIn0.eyJleHAiOjE3OTI0NDIyNTIsImlhdCI6MTc5MjQ0MTM1MiwianRpIjoiZDc1YzczZDUtNjJmNi00NzUzLTgzMmQtNmZjYWNkYTQyOTkzIiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo5MDkxL3JlYWxtcy9ldmVudC10aWNrZXRpbmciLCJhdWQiOlsiaHR0cDovL3RpY2tldCIsImh0dHA6Ly9vcmRlciIsImh0dHA6Ly9wYXltZW50IiwiaHR0cDovL2dhdGV3YXkiXSwic3ViIjoiMjAzNTg2N2QtYzExMi00N2RiLTgwNjktZTgwNDE0NDlmMjA0IiwidHlwIjoiQmVhcmVyIiwiYXpwIjoic3BhLXdlYi1hcHAiLCJzZXNzaW9uX3N0YXRlIjoiOTJmOTk4MmQtNGVlNS00MDQ1LWI4ODEtMGM1NGNiMjA5MGY4IiwiYWNyIjoiMSIsImFsbG93ZWQtb3JpZ2lucyI6WyJodHRwOi8vbG9jYWxob3N0OjgwODMiXSwic2NvcGUiOiJvcGVuaWQgb3JkZXI6cmVhZCBwYXltZW50OndyaXRlIHBheW1lbnQ6cmVhZCB0aWNrZXQ6cmVhZCBlbWFpbCBvcmRlcjp3cml0ZSBnYXRld2F5OnJlYWQgb3JkZXI6ZGVsZXRlIHByb2ZpbGUgcGF5bWVudDpkZWxldGUiLCJzaWQiOiI5MmY5OTgyZC00ZWU1LTQwNDUtYjg4MS0wYzU0Y2IyMDkwZjgiLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwibmFtZSI6IlVzZXIxIEJ1eWVyIiwicHJlZmVycmVkX3VzZXJuYW1lIjoidXNlcjEiLCJnaXZlbl9uYW1lIjoiVXNlcjEiLCJmYW1pbHlfbmFtZSI6IkJ1eWVyIiwiZW1haWwiOiJ1c2VyMUB0ZXN0LmNvbSJ9.atbCbhjOV1xL8psZzd64A9A6oE8n1xjzuOO7bIVDyEpUSM6nWM1TjRwMMeN1rWjsfzlqzMAjb3m3L9VOckFi8BFpAYiRvEF6jhLQ6A1kUWGljbWPjVkrwVd3OBl5OegPor8na0Y3Py0khlEGSUPd_awsiDvTsRMpmM9D-iUgMbY3vCVFtcRNGtRGmz1iiv18nnqmFz6Ro2tMu7odz_JDU3Y7d-qdMNmLgB2ciPPLxVLSZDUBeKVC6PxwgBHfZmtsywfWCuWndmkTZTFk13R-fK6i0ItFCfy6jiJvGhkTzwVoGaqjRy95wn_crWgxd_GXXzkGDeIJ9KFOev3fvLhPZg'
  },
  {
    user: 'user3',
    token: 'eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJpTFVublVsVFJZaHpNZE5jS0ZBdXhrME9lcGdURVpBUks3aEJxQ0U2RDZJIn0.eyJleHAiOjE3OTI0NDIwNjgsImlhdCI6MTc5MjQ0MTE2OCwianRpIjoiOTViZDBlZjItYTE5YS00MjZhLTgzNGYtMTBmODkzZGM2M2FiIiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo5MDkxL3JlYWxtcy9ldmVudC10aWNrZXRpbmciLCJhdWQiOlsiaHR0cDovL3RpY2tldCIsImh0dHA6Ly9vcmRlciIsImh0dHA6Ly9wYXltZW50IiwiaHR0cDovL2dhdGV3YXkiXSwic3ViIjoiNGY2MWViODktYzc3Ni00MGQzLThhNGQtZmVhOGY0ZGNiYmIxIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoic3BhLXdlYi1hcHAiLCJzZXNzaW9uX3N0YXRlIjoiN2YzYzgxYjYtNDM5MC00NDYzLTkyZDUtOTNhNmY0ZjIyM2ExIiwiYWNyIjoiMSIsImFsbG93ZWQtb3JpZ2lucyI6WyJodHRwOi8vbG9jYWxob3N0OjgwODMiXSwic2NvcGUiOiJvcGVuaWQgb3JkZXI6cmVhZCB0aWNrZXQ6ZGVsZXRlIHBheW1lbnQ6d3JpdGUgcGF5bWVudDpyZWFkIHRpY2tldDpyZWFkIGVtYWlsIG9yZGVyOndyaXRlIGdhdGV3YXk6cmVhZCB0aWNrZXQ6d3JpdGUgb3JkZXI6ZGVsZXRlIHByb2ZpbGUgcGF5bWVudDpkZWxldGUiLCJzaWQiOiI3ZjNjODFiNi00MzkwLTQ0NjMtOTJkNS05M2E2ZjRmMjIzYTEiLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwibmFtZSI6IlVzZXIzIEJ1eWVyIGFuZCBTZWxsZXIiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJ1c2VyMyIsImdpdmVuX25hbWUiOiJVc2VyMyIsImZhbWlseV9uYW1lIjoiQnV5ZXIgYW5kIFNlbGxlciIsImVtYWlsIjoidXNlcjNAdGVzdC5jb20ifQ.oTebVXpKauuktilDr-dMCE6HHnTYU--DaaTvqK_60sKD4i2Nl4RpjjSECkpyviHyCK3QpB3se0WWVYP_hv34ed3O9buDUaDqNSqBe_Id28t6hltLFbejuZBsvnf8oFC6QJSdLXJK4eC-ygjBkwdZcgSTY-IttI-D8UnAbaxhMnZhmlv9bRlvrk3-7WnYdADYURE9gt4V4UBa1089rS3hgX3VBPHCQ0tC9nil2B98YmmoMVNt20j9P2J7dRiltlmRj5UUW308n-47ybDjrMlTXHBlyPg3GaYqFAtiU_HcgSWALo11yRtaHkQB-T24RhN9tPtvJdZjo3V8IAe9kx8tMA'
  },
  {
    user: 'user6',
    token: 'eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJpTFVublVsVFJZaHpNZE5jS0ZBdXhrME9lcGdURVpBUks3aEJxQ0U2RDZJIn0.eyJleHAiOjE3OTI0NDIwNjksImlhdCI6MTc5MjQ0MTE2OSwianRpIjoiOTE5ZWQxM2YtZTRjYi00Y2UzLWJkZTctNmI0MjY4MTc4MjMzIiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo5MDkxL3JlYWxtcy9ldmVudC10aWNrZXRpbmciLCJhdWQiOlsiaHR0cDovL3RpY2tldCIsImh0dHA6Ly9vcmRlciIsImh0dHA6Ly9wYXltZW50IiwiaHR0cDovL2dhdGV3YXkiXSwic3ViIjoiYWZjNWRiMmQtZjRmMi00ZTQ2LTk4MTktZDVlYjQwNDg0YzJmIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoic3BhLXdlYi1hcHAiLCJzZXNzaW9uX3N0YXRlIjoiMzBjMzUwZjMtNTg3Zi00MWRkLTg2NjMtM2RlNzE3YzY4YTE2IiwiYWNyIjoiMSIsImFsbG93ZWQtb3JpZ2lucyI6WyJodHRwOi8vbG9jYWxob3N0OjgwODMiXSwic2NvcGUiOiJvcGVuaWQgb3JkZXI6cmVhZCB0aWNrZXQ6ZGVsZXRlIHBheW1lbnQ6d3JpdGUgcGF5bWVudDpyZWFkIHRpY2tldDpyZWFkIGVtYWlsIG9yZGVyOndyaXRlIGdhdGV3YXk6cmVhZCB0aWNrZXQ6d3JpdGUgb3JkZXI6ZGVsZXRlIHByb2ZpbGUgcGF5bWVudDpkZWxldGUiLCJzaWQiOiIzMGMzNTBmMy01ODdmLTQxZGQtODY2My0zZGU3MTdjNjhhMTYiLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwibmFtZSI6IlVzZXI2IEJ1eWVyIGFuZCBTZWxsZXIiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJ1c2VyNiIsImdpdmVuX25hbWUiOiJVc2VyNiIsImZhbWlseV9uYW1lIjoiQnV5ZXIgYW5kIFNlbGxlciIsImVtYWlsIjoidXNlcjZAdGVzdC5jb20ifQ.URyT9hQcTNJzLSm8I1TBOy1wVcNXZAfNWgPzvaBpBHnOKQgccyDNaAa8oWwalxnGFIQQtc3bQWG1DNVBlc5N2hSmPUdhWhfzS2A6a80M9Ssl-_BWrHXExBeHeoXE19UYTZlcoFcVUYO1nnrXZa2WAmJ9jXchTc3vd-orF8EobLfSeYRikOn62lhYkjvXEtQuxWKWIliaBFfvc-B1qzEJv5O0zryRw444XJaaD-K0VHmdLhBLuC1FzObic4zcQbkkgkSXfi4yondxeCf0WSh7kgfsl0QMJhvNATFHW6r2p6dYPDvmoJSrvIP0958BuFe_iraSPiLaoIfqtNUy8bAP-Q'
  }
];
const API_URL = 'http://localhost:8081/graphql';
const HEADERS = {
  'Content-Type': 'application/json',
  'X-Gateway-Secret': 'mygatewaysecret',
  'Authorization': 'Bearer YOUR_TOKEN'
};

const CREATE_MUTATION = `
  mutation CreateOrder {
    create(
      createOrderRequest: {ticketId: "01a155cf-f52a-7df4-a028-23f658b5be0c"}
    ) {
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
`;

async function runSequentialTest(u) {

  console.log(`--- [Iteration for ${u.user}] ---`);
  HEADERS['Authorization'] = `Bearer ${u.token}`;

  try {
    // 1. Call Create Endpoint
    const createResponse = await axios.post(API_URL, {
      query: CREATE_MUTATION,
    }, { headers: HEADERS });

    //{"data":{"create":{"id":"01a113cc-a2f4-7c94-b1e3-b5d375fee827"}}}
    const createdItem = createResponse.data?.data?.create;
    const itemId = createdItem?.id;

    if (!itemId) {
      console.error(`❌ Iteration ${u.user}: Create failed ->`, JSON.stringify(createResponse.data?.errors || createResponse.data));
      return; // Skip to next iteration if creation fails
    }
    console.log(`   Iteration ${u.user} Created ID: ${itemId}`);

  } catch (error) {
    // Catches HTTP errors (like 4xx/5xx network drops)
    console.error(`   💥 Network/HTTP Error on Iteration ${u.user}:`, error.response?.data || error.message);
  }

  // Tiny visual separator between logs
  console.log('');
}

console.log(`Starting sequential test:\n`);

USERS.forEach(u => runSequentialTest(u));


//console.log('Sequential testing complete.');
