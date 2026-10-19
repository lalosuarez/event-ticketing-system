const axios = require('axios');

const TOTAL_RUNS = 200;
const API_URL = 'http://localhost:8082/graphql';
const HEADERS = {
  'Content-Type': 'application/json',
  'X-Gateway-Secret': 'mygatewaysecret',
  'Authorization': 'Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJpTFVublVsVFJZaHpNZE5jS0ZBdXhrME9lcGdURVpBUks3aEJxQ0U2RDZJIn0.eyJleHAiOjE3OTI0NDE3MTIsImlhdCI6MTc5MjQ0MDgxMiwianRpIjoiM2JiNzRiYTEtN2I3My00MTQ2LWIyMjUtYWEyYmFiMjJlYzdmIiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo5MDkxL3JlYWxtcy9ldmVudC10aWNrZXRpbmciLCJhdWQiOlsiaHR0cDovL3RpY2tldCIsImh0dHA6Ly9nYXRld2F5Il0sInN1YiI6ImQ1MGJhYTAzLTQ0NjgtNDY3Ni1hNzk3LTZmNjFlMDUyNGU5OSIsInR5cCI6IkJlYXJlciIsImF6cCI6InNwYS13ZWItYXBwIiwic2Vzc2lvbl9zdGF0ZSI6ImRhM2E5ZTA0LWUzN2MtNDA0Ny04MGU5LTJmMjM4ZDc4YTM3NiIsImFjciI6IjEiLCJhbGxvd2VkLW9yaWdpbnMiOlsiaHR0cDovL2xvY2FsaG9zdDo4MDgzIl0sInNjb3BlIjoib3BlbmlkIHRpY2tldDpyZWFkIGVtYWlsIGdhdGV3YXk6cmVhZCB0aWNrZXQ6ZGVsZXRlIHRpY2tldDp3cml0ZSBwcm9maWxlIiwic2lkIjoiZGEzYTllMDQtZTM3Yy00MDQ3LTgwZTktMmYyMzhkNzhhMzc2IiwiZW1haWxfdmVyaWZpZWQiOnRydWUsIm5hbWUiOiJVc2VyMiBTZWxsZXIiLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJ1c2VyMiIsImdpdmVuX25hbWUiOiJVc2VyMiIsImZhbWlseV9uYW1lIjoiU2VsbGVyIiwiZW1haWwiOiJ1c2VyMkB0ZXN0LmNvbSJ9.bZ5Ok1VsyxBA-VerbxxlUEunINXYpZ-TKf0cVXGbbeK0-8W-e_mpcvSWfnscwVnM7SZRVWkmRAMmYfh21iFdaUup5qpMM09if2cbhFuuqgKFgew53uG9b0DAIRV2MJJpxFr3uN5k2sA_xb5qpjAShsLxSmDsAsqBsEuHfprIr9rRvF40KlXB2a8OxuKyt6vzL7Yvk7cClm2hOu1A7WqJcCH6R6cdlDR1mKwF5frQV8RfyzXNogp-zRryDBdU8bTiJqeEL4qjl37hjISoSPs2od-14PK-8Tu-NzxnpN13s9h6221jbUN_dJcclZ9mQbhxEVYAu6aJwcfyAFzdiFIoVQ'
};

const CREATE_MUTATION = `
  mutation CreateItem($title: String!) {
    create(
      createTicketRequest: {
        title: $title
        price: 5.00
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
