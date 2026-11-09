import Keycloak from 'keycloak-js';

const keycloak = new Keycloak({
  url: 'http://localhost:9091',
  realm: 'event-ticketing',
  clientId: 'spa-web-app',
});

export default keycloak;