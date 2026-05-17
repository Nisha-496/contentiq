import { MessageSquare, ThumbsUp, ThumbsDown, HelpCircle, TrendingUp } from 'lucide-react';
import AnimatedNumber from './AnimatedNumber';

export default function SummaryBar({ total, positive, negative, questions, neutralCount }) {
  const pct = (n) => (total === 0 ? 0 : (100 * n) / total);

  const items = [
    {
      label: 'Total comments',
      value: total,
      pct: null,
      color: 'text-slate-200',
      iconColor: 'text-slate-300',
      bg: 'from-slate-700/60 to-slate-800/40',
      glow: '',
      icon: <MessageSquare className="w-5 h-5" />,
    },
    {
      label: 'Positive',
      value: positive,
      pct: pct(positive),
      color: 'text-emerald-300',
      iconColor: 'text-emerald-300',
      bg: 'from-emerald-500/20 to-emerald-700/10',
      glow: 'hover:shadow-glow-emerald',
      icon: <ThumbsUp className="w-5 h-5" />,
    },
    {
      label: 'Negative',
      value: negative,
      pct: pct(negative),
      color: 'text-rose-300',
      iconColor: 'text-rose-300',
      bg: 'from-rose-500/20 to-rose-700/10',
      glow: 'hover:shadow-glow-rose',
      icon: <ThumbsDown className="w-5 h-5" />,
    },
    {
      label: 'Questions',
      value: questions,
      pct: pct(questions),
      color: 'text-amber-300',
      iconColor: 'text-amber-300',
      bg: 'from-amber-500/20 to-amber-700/10',
      glow: 'hover:shadow-glow-amber',
      icon: <HelpCircle className="w-5 h-5" />,
    },
  ];

  return (
    <div className="glass-strong rounded-2xl p-6 animate-slide-up overflow-hidden relative">
      <div className="absolute -top-24 -right-24 w-64 h-64 bg-brand-500/10 rounded-full blur-3xl pointer-events-none" />

      <div className="flex items-center gap-2 mb-5 relative">
        <TrendingUp className="w-4 h-4 text-brand-400" />
        <h3 className="text-xs uppercase tracking-[0.18em] font-semibold text-slate-400">
          Live breakdown
        </h3>
      </div>

      <div className="grid grid-cols-2 md:grid-cols-4 gap-3 relative">
        {items.map((it, idx) => (
          <div
            key={it.label}
            className={`group relative rounded-xl border border-slate-800/60 bg-gradient-to-br ${it.bg}
                       p-4 transition-all duration-300 hover:-translate-y-0.5 ${it.glow}
                       animate-scale-in stagger-${idx + 1}`}
          >
            <div className="flex items-center gap-3 mb-3">
              <div
                className={`w-9 h-9 rounded-lg bg-slate-950/50 border border-slate-800/80
                           ${it.iconColor} flex items-center justify-center shrink-0
                           group-hover:scale-110 transition-transform duration-300`}
              >
                {it.icon}
              </div>
              <div className="text-[10px] uppercase tracking-wider font-semibold text-slate-500">
                {it.label}
              </div>
            </div>
            <div className="flex items-baseline gap-2">
              <AnimatedNumber
                value={it.value}
                className={`text-3xl font-bold ${it.color} tracking-tight`}
              />
              {it.pct !== null && (
                <span className={`text-sm font-semibold ${it.color} opacity-80`}>
                  {it.pct.toFixed(1)}%
                </span>
              )}
            </div>
          </div>
        ))}
      </div>

      {total > 0 && (
        <div className="mt-5 relative">
          <div className="h-2.5 rounded-full bg-slate-800/60 overflow-hidden flex shadow-inner">
            <div
              className="h-full bg-gradient-to-r from-emerald-400 to-emerald-500 transition-all duration-700"
              style={{ width: `${pct(positive)}%` }}
              title={`Positive ${pct(positive).toFixed(1)}%`}
            />
            <div
              className="h-full bg-gradient-to-r from-amber-400 to-amber-500 transition-all duration-700"
              style={{ width: `${pct(questions)}%` }}
              title={`Questions ${pct(questions).toFixed(1)}%`}
            />
            <div
              className="h-full bg-gradient-to-r from-rose-400 to-rose-500 transition-all duration-700"
              style={{ width: `${pct(negative)}%` }}
              title={`Negative ${pct(negative).toFixed(1)}%`}
            />
            <div
              className="h-full bg-gradient-to-r from-slate-600 to-slate-700 transition-all duration-700"
              style={{ width: `${pct(neutralCount || 0)}%` }}
              title={`Neutral ${pct(neutralCount || 0).toFixed(1)}%`}
            />
          </div>
          <div className="flex justify-between mt-2 text-[10px] uppercase tracking-wider text-slate-500">
            <span>Sentiment distribution</span>
            <span>{total.toLocaleString()} total</span>
          </div>
        </div>
      )}
    </div>
  );
}
