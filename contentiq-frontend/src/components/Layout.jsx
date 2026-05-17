import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Brain, History, LogOut, LayoutDashboard } from 'lucide-react';

export default function Layout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const navClass = ({ isActive }) =>
    `relative inline-flex items-center gap-1.5 px-3 py-2 rounded-lg text-sm font-medium transition-all duration-200 ${
      isActive
        ? 'text-white bg-slate-800/70'
        : 'text-slate-400 hover:text-white hover:bg-slate-800/40'
    }`;

  return (
    <div className="min-h-screen flex flex-col">
      <header className="sticky top-0 z-30 border-b border-slate-800/60 bg-slate-950/70 backdrop-blur-xl">
        <div className="absolute inset-x-0 bottom-0 h-px bg-gradient-to-r from-transparent via-brand-500/40 to-transparent" />
        <div className="max-w-7xl mx-auto px-6 py-3 flex items-center justify-between gap-4">
          <Link to="/" className="flex items-center gap-2.5 group shrink-0">
            <div
              className="w-9 h-9 rounded-xl bg-gradient-to-br from-brand-500 via-brand-600 to-brand-800
                         flex items-center justify-center shadow-glow
                         group-hover:scale-105 transition-transform duration-300"
            >
              <Brain className="w-5 h-5 text-white" />
            </div>
            <div className="flex flex-col leading-none">
              <span className="font-bold text-lg tracking-tight text-white">ContentIQ</span>
              <span className="text-[10px] uppercase tracking-[0.18em] text-slate-500 font-medium">
                audience intelligence
              </span>
            </div>
          </Link>

          <nav className="flex items-center gap-1">
            <NavLink to="/" end className={navClass}>
              <LayoutDashboard className="w-4 h-4" />
              <span className="hidden sm:inline">Dashboard</span>
            </NavLink>
            <NavLink to="/history" className={navClass}>
              <History className="w-4 h-4" />
              <span className="hidden sm:inline">History</span>
            </NavLink>
          </nav>

          <div className="flex items-center gap-3 shrink-0">
            {user?.username && (
              <div className="hidden sm:flex items-center gap-2.5 pl-3 pr-1 py-1 rounded-full
                             bg-slate-900/60 border border-slate-800">
                <div
                  className="w-7 h-7 rounded-full bg-gradient-to-br from-brand-500 to-purple-600
                            flex items-center justify-center text-white font-bold text-xs uppercase"
                >
                  {user.username[0]}
                </div>
                <span className="text-sm text-slate-200 font-medium">{user.username}</span>
                <button
                  onClick={handleLogout}
                  title="Sign out"
                  className="w-7 h-7 rounded-full hover:bg-slate-800 flex items-center justify-center text-slate-400 hover:text-rose-300 transition-colors"
                >
                  <LogOut className="w-3.5 h-3.5" />
                </button>
              </div>
            )}
            <button onClick={handleLogout} className="sm:hidden btn-ghost">
              <LogOut className="w-4 h-4" />
            </button>
          </div>
        </div>
      </header>

      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 py-6 sm:py-8">
        <Outlet />
      </main>

      <footer className="border-t border-slate-900 py-5 text-center text-xs text-slate-600">
        <span className="opacity-70">ContentIQ</span> · YouTube content intelligence
      </footer>
    </div>
  );
}
