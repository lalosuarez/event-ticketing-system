const axios = require('axios');

const TOTAL_RUNS = 200;
const API_URL = 'http://localhost:8082/graphql';
const HEADERS = {
  'Content-Type': 'application/json',
  // 'Authorization': 'Bearer YOUR_TOKEN'
};

const CREATE_MUTATION = `
  mutation CreateItem($title: String!) {
    create(
      createTicketRequest: {
        title: $title
        price: 5.00
        userId: "user1"
      }
    ) { id }
  }
`;

const UPDATE_MUTATION = `
   mutation MyMutation($id: ID!, $title: String!, $price: BigDecimal!) {
    update(
      updateTicketRequest: {
        id: $id
        title: $title
        price: $price
        userId: "user1"
      }
    ) { price }
  }
`;

async function runSequentialTest(i) {

  // for (let i = 1; i <= TOTAL_RUNS; i++) {
     console.log(`--- [Iteration ${i}/${TOTAL_RUNS}] ---`);

    try {
      // 1. Call Create Endpoint
      const createResponse = await axios.post(API_URL, {
        query: CREATE_MUTATION,
        variables: {
          title: `Concert ${i}`
        }
      }, { headers: HEADERS });

      //{"data":{"create":{"id":"01a113cc-a2f4-7c94-b1e3-b5d375fee827"}}}
      const createdItem = createResponse.data?.data?.create;
      const itemId = createdItem?.id;

      if (!itemId) {
        console.error(`❌ Iteration ${i}: Create failed ->`, JSON.stringify(createResponse.data?.errors || createResponse.data));
        return; // Skip to next iteration if creation fails
      }
      console.log(`   Created ID: ${itemId}`);

      // 2. Call Update Endpoint to update the price to 10
      const updateResponse = await axios.post(API_URL, {
        query: UPDATE_MUTATION,
        variables: {
          id: `${itemId}`,
          title: `Concert ${i}`,
          price: 10.00
        }
      }, { headers: HEADERS });

      const updatedItem = updateResponse.data?.data?.update;

      if (updateResponse.data?.errors) {
        console.error(`   ${itemId} ❌ Update failed ->`, JSON.stringify(updateResponse.data.errors));
      } else {
        console.log(`   ${itemId} ✅ Update success -> New price: ${updatedItem?.price}`);
      }

      // 3. Call Update Endpoint to update the price to 15
      const updateResponse2 = await axios.post(API_URL, {
        query: UPDATE_MUTATION,
        variables: {
          id: `${itemId}`,
          title: `Concert ${i}`,
          price: 15.00
        }
      }, { headers: HEADERS });

      const updatedItem2 = updateResponse2.data?.data?.update;

      if (updateResponse2.data?.errors) {
        console.error(`   ${itemId} ❌ Update failed ->`, JSON.stringify(updateResponse2.data.errors));
      } else {
        console.log(`   ${itemId} ✅ Update success -> New price: ${updatedItem2?.price}`);
      }

    } catch (error) {
      // Catches HTTP errors (like 4xx/5xx network drops)
      console.error(`   💥 Network/HTTP Error on Iteration ${i}:`, error.response?.data || error.message);
    }

    // Tiny visual separator between logs
    console.log('');

  // }
}

console.log(`Starting sequential test: ${TOTAL_RUNS} iterations...\n`);

for (let i = 1; i <= TOTAL_RUNS; i++) {
  runSequentialTest(i);
}

//console.log('Sequential testing complete.');
