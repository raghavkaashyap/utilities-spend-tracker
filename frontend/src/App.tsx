import { BrowserRouter, Routes, Route, Link, Navigate } from "react-router-dom";
import Login from "./pages/Login";
import Signup from "./pages/Signup";
import BillsPage from "./pages/BillsPage";
import Summary from "./pages/Summary";
import { logout } from "./services/authService";

function App() {
  const token = localStorage.getItem("token");

  const onLogout = () => {
    logout();
    window.location.href = "/login";
  };

  return (
    <BrowserRouter>
      <header className="bg-white shadow-md">
        <nav className="container mx-auto px-6 py-4 flex justify-between items-center">
          <Link to="/" className="text-xl font-semibold text-gray-800">
            SpendTracker
          </Link>
          <div className="flex items-center space-x-4">
            {token && (
              <>
                <Link
                  to="/"
                  className="px-3 py-2 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-100"
                >
                  Bills
                </Link>
                <Link
                  to="/summary"
                  className="px-3 py-2 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-100"
                >
                  Summary
                </Link>
              </>
            )}
          </div>
          <div className="flex items-center">
            {token ? (
              <button
                onClick={onLogout}
                className="px-3 py-2 rounded-md text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700"
              >
                Logout
              </button>
            ) : (
              <div className="space-x-2">
                <Link
                  to="/login"
                  className="px-3 py-2 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-100"
                >
                  Login
                </Link>
                <Link
                  to="/signup"
                  className="px-3 py-2 rounded-md text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700"
                >
                  Signup
                </Link>
              </div>
            )}
          </div>
        </nav>
      </header>

      <main>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/signup" element={<Signup />} />
          <Route
            path="/"
            element={token ? <BillsPage /> : <Navigate to="/login" replace />}
          />
          <Route
            path="/summary"
            element={token ? <Summary /> : <Navigate to="/login" replace />}
          />
        </Routes>
      </main>
    </BrowserRouter>
  );
}

export default App;
