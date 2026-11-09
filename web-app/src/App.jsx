import { useAuth } from "./AuthContext";
import "./App.css";

function App() {
  const { authenticated, login, logout, isKeycloakAvailable, keycloak, token } =
    useAuth();

  return (
    <div className="min-h-screen bg-slate-50 text-slate-800">
      {/* Top Navbar */}
      <header className="border-b border-slate-200 bg-white shadow-xs">
        <div className="max-w-5xl mx-auto px-6 py-4 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <span className="font-bold text-lg text-slate-900 tracking-tight">
              Portal
            </span>
            {!isKeycloakAvailable && (
              <span className="text-xs px-2 py-0.5 rounded-full bg-amber-50 text-amber-700 border border-amber-200">
                Auth Offline (Guest Mode)
              </span>
            )}
          </div>

          <div>
            {authenticated ? (
              <div className="flex items-center gap-3">
                <span className="text-sm font-medium text-slate-700">
                  {keycloak?.tokenParsed?.preferred_username || "Logged In"}
                </span>
                <button
                  type="button"
                  onClick={logout}
                  className="px-3.5 py-1.5 text-sm font-medium rounded-lg bg-red-50 text-red-700 border border-red-200 hover:bg-red-100 transition cursor-pointer"
                >
                  Log Out
                </button>
              </div>
            ) : (
              <button
                type="button"
                onClick={login}
                className="px-4 py-2 text-sm font-semibold rounded-lg bg-indigo-600 text-white hover:bg-indigo-500 shadow-sm shadow-indigo-200 transition cursor-pointer active:scale-95"
              >
                Log In
              </button>
            )}
          </div>
        </div>
      </header>

      {/* Main Content: Renders immediately */}
      <main className="max-w-5xl mx-auto p-6 space-y-6">
        <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-sm">
          <h1 className="text-2xl font-bold text-slate-900 mb-2">
            Public Dashboard
          </h1>
          <p className="text-slate-600 leading-relaxed">
            This page renders instantly regardless of whether the Keycloak
            identity server is running. Users can browse documentation,
            products, and articles freely.
          </p>
        </div>

        {authenticated && (
          <div className="bg-emerald-50 border border-emerald-200 rounded-xl p-4">
            <h2 className="font-semibold text-emerald-900">Protected Token</h2>
            <p className="text-xs font-mono text-emerald-800 break-all mt-1">
              {token}
            </p>
          </div>
        )}
      </main>
    </div>
  );
}

export default App;
