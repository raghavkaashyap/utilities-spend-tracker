import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { LogOut, LayoutDashboard, FileText, BarChart2 } from 'lucide-react';
import ThemeToggle from './ThemeToggle';
import { logout } from '../services/authService';

const Layout = () => {
  const navigate = useNavigate();

  const onLogout = () => {
    logout();
    navigate('/login');
  };

  const navItems = [
    { to: '/', icon: <LayoutDashboard size={24} />, label: 'Bills' },
    { to: '/summary', icon: <BarChart2 size={24} />, label: 'Summary' },
  ];

  return (
    <div className="flex h-screen bg-base-200">
      {/* Sidebar for desktop */}
      <aside className="hidden md:flex flex-col w-64 bg-base-100 shadow-lg">
        <div className="flex items-center justify-center h-20 shadow-md">
          <FileText className="h-8 w-8 text-primary" />
          <h1 className="text-2xl font-bold ml-2 text-text-base">SpendTracker</h1>
        </div>
        <nav className="flex-1 px-4 py-8 space-y-2">
          {navItems.map((item) => (
            <NavLink
              key={item.label}
              to={item.to}
              className={({ isActive }) =>
                `flex items-center px-4 py-3 text-lg font-medium rounded-lg transition-colors ${
                  isActive
                    ? 'bg-primary text-white'
                    : 'text-text-muted hover:bg-base-200'
                }`
              }
            >
              {item.icon}
              <span className="ml-4">{item.label}</span>
            </NavLink>
          ))}
        </nav>
        <div className="p-4 border-t border-base-300">
          <ThemeToggle />
          <button
            onClick={onLogout}
            className="w-full flex items-center mt-4 px-4 py-3 text-lg font-medium rounded-lg text-text-muted hover:bg-red-500 hover:text-white transition-colors"
          >
            <LogOut size={24} />
            <span className="ml-4">Logout</span>
          </button>
        </div>
      </aside>

      <div className="flex-1 flex flex-col overflow-hidden">
        <main className="flex-1 overflow-x-hidden overflow-y-auto bg-base-200 p-4 sm:p-6 lg:p-8">
          <Outlet />
        </main>
      </div>

      {/* Bottom navigation for mobile */}
      <footer className="md:hidden fixed bottom-0 left-0 right-0 bg-base-100 shadow-t-lg">
        <nav className="flex justify-around items-center h-16">
          {navItems.map((item) => (
            <NavLink
              key={item.label}
              to={item.to}
              className={({ isActive }) =>
                `flex flex-col items-center justify-center w-full h-full transition-colors ${
                  isActive ? 'text-primary' : 'text-text-muted'
                }`
              }
            >
              {item.icon}
              <span className="text-xs mt-1">{item.label}</span>
            </NavLink>
          ))}
          <button
            onClick={onLogout}
            className="flex flex-col items-center justify-center w-full h-full text-text-muted transition-colors"
          >
            <LogOut size={24} />
            <span className="text-xs mt-1">Logout</span>
          </button>
        </nav>
      </footer>
    </div>
  );
};

export default Layout;
