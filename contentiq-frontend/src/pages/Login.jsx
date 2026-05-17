import { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  Brain, Mail, User, Lock, ArrowRight, Loader2,
  Sparkles, Youtube, MessageSquare, FileText,
} from 'lucide-react';
import toast from 'react-hot-toast';

export default function Login() {
  const [mode, setMode] = useState('login');
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);

  const { login, register } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const from = location.state?.from?.pathname || '/';

  const submit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      if (mode === 'login') {
        await login(username, password);
        toast.success(`Welcome back, ${username}!`);
      } else {
        await register(username, email, password);
        toast.success(`Account created for ${username}`);
      }
      navigate(from, { replace: true });
    } catch (err) {
      const msg =
        err.response?.data?.message ||
        err.response?.data?.error ||
        (err.response?.status === 401 ? 'Invalid credentials' : 'Something went wrong');
      toast.error(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen relative overflow-hidden flex items-center justify-center px-4 py-12">
      <div className="absolute inset-0 bg-auth-aurora animate-aurora pointer-events-none" />
      <div className="absolute inset-0 bg-[url('data:image/svg+xml;utf8,<svg%20xmlns=%22http://www.w3.org/2000/svg%22%20width=%2240%22%20height=%2240%22><circle%20cx=%221%22%20cy=%221%22%20r=%221%22%20fill=%22rgba(255,255,255,0.04)%22/></svg>')] opacity-50 pointer-events-none" />

      <div className="relative w-full max-w-5xl grid md:grid-cols-2 gap-12 items-center">
        <div className="hidden md:block animate-slide-up">
          <div className="flex items-center gap-2 mb-6">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-brand-500 to-brand-700 flex items-center justify-center shadow-glow">
              <Brain className="w-5 h-5 text-white" />
            </div>
            <span className="text-xl font-bold tracking-tight text-white">ContentIQ</span>
          </div>

          <h1 className="text-4xl lg:text-5xl font-extrabold tracking-tight leading-[1.05] text-balance mb-4">
            <span className="gradient-text">Understand your</span>{' '}
            <span className="brand-text">YouTube audience</span>{' '}
            <span className="gradient-text">in seconds.</span>
          </h1>
          <p className="text-base text-slate-400 text-pretty mb-8 max-w-md">
            Real-time sentiment classification on every comment of any video, powered by transformer models and presented in a dashboard you'll actually want to look at.
          </p>

          <div className="space-y-3">
            <Bullet icon={<Youtube className="w-4 h-4 text-rose-400" />} text="Import from any YouTube URL" />
            <Bullet icon={<MessageSquare className="w-4 h-4 text-emerald-400" />} text="Per-comment sentiment + confidence score" />
            <Bullet icon={<FileText className="w-4 h-4 text-amber-400" />} text="PDF reports + history of every analysis" />
          </div>
        </div>

        <div className="w-full max-w-md mx-auto animate-scale-in">
          <div className="md:hidden flex flex-col items-center mb-6">
            <div className="w-14 h-14 rounded-2xl bg-gradient-to-br from-brand-500 to-brand-700 flex items-center justify-center shadow-glow mb-3">
              <Brain className="w-7 h-7 text-white" />
            </div>
            <h1 className="text-2xl font-bold tracking-tight text-white">ContentIQ</h1>
            <p className="text-slate-400 text-sm mt-1">YouTube comment intelligence</p>
          </div>

          <div className="glass-strong rounded-2xl p-7 relative overflow-hidden">
            <div className="absolute -top-24 -right-24 w-48 h-48 bg-brand-500/15 rounded-full blur-3xl pointer-events-none" />

            <div className="relative">
              <div className="flex items-center gap-2 mb-1.5">
                <Sparkles className="w-4 h-4 text-brand-400" />
                <span className="text-xs uppercase tracking-[0.18em] font-semibold text-brand-400">
                  {mode === 'login' ? 'Welcome back' : 'Get started'}
                </span>
              </div>
              <h2 className="text-2xl font-bold text-white tracking-tight mb-5">
                {mode === 'login' ? 'Sign in to ContentIQ' : 'Create your account'}
              </h2>

              <div className="flex gap-1 mb-6 p-1 bg-slate-950/60 rounded-xl border border-slate-800">
                <button
                  type="button"
                  onClick={() => setMode('login')}
                  className={`flex-1 py-2 rounded-lg text-sm font-semibold transition-all ${
                    mode === 'login'
                      ? 'bg-gradient-to-br from-slate-700 to-slate-800 text-white shadow-md'
                      : 'text-slate-400 hover:text-white'
                  }`}
                >
                  Sign in
                </button>
                <button
                  type="button"
                  onClick={() => setMode('register')}
                  className={`flex-1 py-2 rounded-lg text-sm font-semibold transition-all ${
                    mode === 'register'
                      ? 'bg-gradient-to-br from-slate-700 to-slate-800 text-white shadow-md'
                      : 'text-slate-400 hover:text-white'
                  }`}
                >
                  Create account
                </button>
              </div>

              <form onSubmit={submit} className="space-y-4">
                <Field icon={<User className="w-4 h-4" />} label="Username">
                  <input
                    type="text"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    className="input"
                    placeholder="your-handle"
                    required
                    minLength={3}
                    autoComplete="username"
                  />
                </Field>

                {mode === 'register' && (
                  <Field icon={<Mail className="w-4 h-4" />} label="Email">
                    <input
                      type="email"
                      value={email}
                      onChange={(e) => setEmail(e.target.value)}
                      className="input"
                      placeholder="you@example.com"
                      required
                      autoComplete="email"
                    />
                  </Field>
                )}

                <Field icon={<Lock className="w-4 h-4" />} label="Password">
                  <input
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    className="input"
                    placeholder="••••••••"
                    required
                    minLength={6}
                    autoComplete={mode === 'login' ? 'current-password' : 'new-password'}
                  />
                </Field>

                <button type="submit" disabled={loading} className="btn-primary w-full mt-6">
                  {loading ? (
                    <>
                      <Loader2 className="w-4 h-4 animate-spin" />
                      Please wait...
                    </>
                  ) : (
                    <>
                      {mode === 'login' ? 'Sign in' : 'Create account'}
                      <ArrowRight className="w-4 h-4" />
                    </>
                  )}
                </button>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

function Field({ icon, label, children }) {
  return (
    <label className="block">
      <span className="flex items-center gap-2 text-xs font-semibold text-slate-400 mb-1.5 uppercase tracking-wider">
        <span className="text-slate-500">{icon}</span>
        {label}
      </span>
      {children}
    </label>
  );
}

function Bullet({ icon, text }) {
  return (
    <div className="flex items-center gap-3 text-sm text-slate-300">
      <span className="w-8 h-8 rounded-lg bg-slate-900/60 border border-slate-800 flex items-center justify-center shrink-0">
        {icon}
      </span>
      {text}
    </div>
  );
}
