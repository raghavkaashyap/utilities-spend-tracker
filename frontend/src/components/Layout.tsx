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
    <div className="flex min-h-screen bg-base-200 subtle-grid">
      <aside className="hidden md:flex md:sticky md:top-0 md:h-screen flex-col w-72 m-4 mr-0 rounded-3xl glass-card overflow-hidden">
        <div className="flex items-center px-6 h-20 border-b border-base-300/60">
          <FileText className="h-7 w-7 text-primary" />
          <h1 className="text-xl font-semibold ml-3 tracking-tight text-text-base">SpendTracker</h1>
        </div>
        <nav className="flex-1 px-4 py-8 space-y-2">
          {navItems.map((item) => (
            <NavLink
              key={item.label}
              to={item.to}
              className={({ isActive }) =>
                `flex items-center px-4 py-3 text-base font-medium rounded-2xl transition-all ${
                  isActive
                    ? 'bg-primary text-white shadow-[0_8px_20px_-10px_rgba(0,113,227,0.8)]'
                    : 'text-text-muted hover:bg-base-200/80 hover:text-text-base'
                }`
              }
            >
              {item.icon}
              <span className="ml-3">{item.label}</span>
            </NavLink>
          ))}
        </nav>
        <div className="p-4 border-t border-base-300/60">
          <ThemeToggle />
          <button
            onClick={onLogout}
            className="w-full flex cursor-pointer items-center mt-4 px-4 py-3 text-base font-medium rounded-2xl text-text-muted hover:bg-red-500 hover:text-white transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-red-500/40"
          >
            <LogOut size={24} />
            <span className="ml-3">Logout</span>
          </button>
        </div>
      </aside>

      <div className="flex-1 flex flex-col overflow-hidden">
        <main className="flex-1 overflow-x-hidden overflow-y-auto p-4 sm:p-6 lg:p-10 pb-24 md:pb-10">
          <Outlet />
        </main>
      </div>

      <footer className="md:hidden fixed bottom-4 left-4 right-4 z-40">
        <nav className="glass-card rounded-2xl flex justify-around items-center h-16">
          {navItems.map((item) => (
            <NavLink
              key={item.label}
              to={item.to}
              className={({ isActive }) =>
                `flex flex-col items-center justify-center w-full h-full transition-colors ${
                  isActive ? 'text-primary' : 'text-text-muted hover:text-text-base'
                }`
              }
            >
              {item.icon}
              <span className="text-xs mt-1">{item.label}</span>
            </NavLink>
          ))}
          <button
            onClick={onLogout}
            className="flex flex-col cursor-pointer items-center justify-center w-full h-full text-text-muted transition-colors hover:text-red-500 focus:outline-none focus-visible:ring-2 focus-visible:ring-red-500/40"
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
