const axios = require('axios');

const USERS = [
  {
    user: 'user1',
    token: ''
  },
  {
    user: 'user3',
    token: ''
  },
  {
    user: 'user6',
    token: ''
  }
];
const TICKET_ID = '';
const API_URL = 'http://localhost:8081/graphql';
const HEADERS = {
  'Content-Type': 'application/json',
  'X-Gateway-Secret': 'mygatewaysecret',
  'Authorization': 'Bearer YOUR_TOKEN'
};

const CREATE_MUTATION = `
  mutation CreateOrder {
    create(
      createOrderRequest: {ticketId: "${TICKET_ID}"}
    ) {
      id
      totalAmount
      status
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
