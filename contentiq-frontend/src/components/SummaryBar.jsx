import { MessageSquare, ThumbsUp, ThumbsDown, HelpCircle } from 'lucide-react';

export default function SummaryBar({ total, positive, negative, questions, neutralCount }) {
  const pct = (n) => (total === 0 ? 0 : (100 * n) / total);

  const items = [
    {
      label: 'Total comments',
      value: total,
      pct: null,
      color: 'text-slate-300',
      bg: 'bg-slate-700/40',
      icon: <MessageSquare className="w-4 h-4" />,
    },
    {
      label: 'Positive',
      value: positive,
      pct: pct(positive),
      color: 'text-emerald-400',
      bg: 'bg-emerald-500/15',
      icon: <ThumbsUp className="w-4 h-4" />,
    },
    {
      label: 'Negative',
      value: negative,
      pct: pct(negative),
      color: 'text-rose-400',
      bg: 'bg-rose-500/15',
      icon: <ThumbsDown className="w-4 h-4" />,
    },
    {
      label: 'Questions',
      value: questions,
      pct: pct(questions),
      color: 'text-amber-400',
      bg: 'bg-amber-500/15',
      icon: <HelpCircle className="w-4 h-4" />,
    },
  ];

  return (
    <div className="glass rounded-2xl p-5 animate-slide-up">
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        {items.map((it) => (
          <div
            key={it.label}
            className="flex items-center gap-3 px-3 py-2 rounded-xl bg-slate-900/40 border border-slate-800/40"
          >
            <div className={`w-9 h-9 rounded-lg ${it.bg} ${it.color} flex items-center justify-center shrink-0`}>
              {it.icon}
            </div>
            <div className="min-w-0">
              <div className="text-[11px] text-slate-500 uppercase tracking-wider">{it.label}</div>
              <div className="flex items-baseline gap-2">
                <span className="text-xl font-bold text-white tabular-nums">{it.value}</span>
                {it.pct !== null && (
                  <span className={`text-xs font-medium ${it.color} tabular-nums`}>
                    {it.pct.toFixed(1)}%
                  </span>
                )}
              </div>
            </div>
          </div>
        ))}
      </div>

      {total > 0 && (
        <div className="mt-4 h-2 rounded-full bg-slate-800/60 overflow-hidden flex">
          <div className="h-full bg-emerald-500" style={{ width: `${pct(positive)}%` }} />
          <div className="h-full bg-amber-500" style={{ width: `${pct(questions)}%` }} />
          <div className="h-full bg-rose-500" style={{ width: `${pct(negative)}%` }} />
          <div
            className="h-full bg-slate-600"
            style={{ width: `${pct(neutralCount || 0)}%` }}
          />
        </div>
      )}
    </div>
  );
}
