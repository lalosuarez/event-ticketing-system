import { createContext, useContext, useEffect, useState, useRef } from 'react';
import keycloak from './keycloak';

const AuthContext = createContext({
  keycloak: null,
  authenticated: false,
  token: null,
  login: () => {},
  logout: () => {},
});

export function AuthProvider({ children }) {
  const [authenticated, setAuthenticated] = useState(false);
  const [token, setToken] = useState(null);
  const isInitializing = useRef(false);

  useEffect(() => {
    // Check if the current URL contains OAuth callback parameters (after a successful login)
    const hasAuthParams =
      window.location.search.includes('code=') ||
      window.location.search.includes('state=') ||
      window.location.hash.includes('access_token=');

    // ONLY initialize automatically if returning from Keycloak login redirect
    if (hasAuthParams && !isInitializing.current) {
      isInitializing.current = true;
      keycloak
        .init({
          pkceMethod: 'S256',
          checkLoginIframe: false,
        })
        .then((auth) => {
          setAuthenticated(Boolean(auth));
          if (auth) {
            setToken(keycloak.token);
          }
          // Strip OAuth query/hash parameters to clean up the URL
          window.history.replaceState({}, document.title, window.location.pathname);
        })
        .catch((err) => {
          console.error('Failed to parse authentication callback:', err);
        });
    }
  }, []);

  const login = async () => {
    // 1. Verify Keycloak server is actually reachable before redirecting
    try {
      const controller = new AbortController();
      const timeoutId = setTimeout(() => controller.abort(), 1200);

      const serverUrl = keycloak.authServerUrl || 'http://localhost:8080';
      const checkUrl = `${serverUrl}/realms/${keycloak.realm}/.well-known/openid-configuration`;

      await fetch(checkUrl, {
        signal: controller.signal,
        mode: 'no-cors',
      });
      clearTimeout(timeoutId);
    } catch {
      alert('Keycloak server is not running or unreachable at http://localhost:8080');
      return;
    }

    // 2. If Keycloak is up, initialize and start the login redirect
    if (!keycloak.didInitialize) {
      try {
        await keycloak.init({
          pkceMethod: 'S256',
          checkLoginIframe: false,
        });
      } catch (err) {
        console.error('Initialization before login failed:', err);
        return;
      }
    }

    keycloak.login();
  };

  const logout = () => {
    if (keycloak.authenticated) {
      keycloak.logout({ redirectUri: window.location.origin });
    }
  };

  return (
    <AuthContext.Provider
      value={{
        keycloak,
        authenticated,
        token,
        login,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);