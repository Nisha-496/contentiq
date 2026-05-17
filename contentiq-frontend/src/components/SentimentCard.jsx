import { useState } from 'react';
import { ChevronDown, Heart } from 'lucide-react';

const themes = {
  positive: {
    border: 'border-emerald-500/30 hover:border-emerald-400/50',
    bg: 'from-emerald-500/[0.07] via-transparent to-transparent',
    accent: 'text-emerald-300',
    badge: 'bg-emerald-500/20 text-emerald-200 border-emerald-500/40',
    headerGradient: 'from-emerald-500/10 via-emerald-500/5 to-transparent',
    pillBg: 'bg-emerald-500/15 border-emerald-500/30',
    glow: 'hover:shadow-glow-emerald',
    barGradient: 'from-emerald-400 to-emerald-600',
  },
  negative: {
    border: 'border-rose-500/30 hover:border-rose-400/50',
    bg: 'from-rose-500/[0.07] via-transparent to-transparent',
    accent: 'text-rose-300',
    badge: 'bg-rose-500/20 text-rose-200 border-rose-500/40',
    headerGradient: 'from-rose-500/10 via-rose-500/5 to-transparent',
    pillBg: 'bg-rose-500/15 border-rose-500/30',
    glow: 'hover:shadow-glow-rose',
    barGradient: 'from-rose-400 to-rose-600',
  },
  question: {
    border: 'border-amber-500/30 hover:border-amber-400/50',
    bg: 'from-amber-500/[0.07] via-transparent to-transparent',
    accent: 'text-amber-300',
    badge: 'bg-amber-500/20 text-amber-200 border-amber-500/40',
    headerGradient: 'from-amber-500/10 via-amber-500/5 to-transparent',
    pillBg: 'bg-amber-500/15 border-amber-500/30',
    glow: 'hover:shadow-glow-amber',
    barGradient: 'from-amber-400 to-amber-600',
  },
};

export default function SentimentCard({ kind, icon, title, comments, emptyMessage }) {
  const t = themes[kind];
  const [expanded, setExpanded] = useState(8);

  const total = comments.length;
  const visible = comments.slice(0, expanded);
  const avgConf =
    total === 0
      ? 0
      : comments.reduce((s, c) => s + (c.confidence || 0), 0) / total;

  return (
    <div
      className={`glass rounded-2xl border ${t.border} bg-gradient-to-b ${t.bg}
                  overflow-hidden flex flex-col group transition-all duration-300 ${t.glow}`}
    >
      <div
        className={`px-5 py-4 border-b border-slate-800/60 bg-gradient-to-r ${t.headerGradient} relative`}
      >
        <div className="flex items-center justify-between mb-1">
          <div className="flex items-center gap-2.5">
            <span
              className={`w-9 h-9 rounded-xl bg-slate-950/50 border border-slate-800/80
                          flex items-center justify-center ${t.accent}
                          group-hover:scale-110 transition-transform duration-300`}
            >
              {icon}
            </span>
            <h3 className="font-bold text-white tracking-tight text-lg">{title}</h3>
          </div>
          <span
            className={`text-xs px-2.5 py-1 rounded-full border ${t.badge} font-semibold tabular-nums shadow-sm`}
          >
            {total}
          </span>
        </div>
        {total > 0 && (
          <div className="flex items-center gap-1.5 text-[11px] text-slate-500 ml-11">
            <span>avg confidence</span>
            <span className={`font-semibold ${t.accent} tabular-nums`}>
              {(avgConf * 100).toFixed(0)}%
            </span>
          </div>
        )}
      </div>

      <div className="p-3 space-y-2 max-h-[480px] overflow-y-auto flex-1">
        {total === 0 ? (
          <div className="text-center text-slate-500 text-sm py-16 px-4">
            <div className="text-4xl opacity-20 mb-3">∅</div>
            {emptyMessage || 'No comments in this category.'}
          </div>
        ) : (
          visible.map((c, i) => (
            <article
              key={c.id || i}
              className={`rounded-xl bg-slate-900/70 border border-slate-800/60 p-3.5
                          hover:border-slate-700 hover:bg-slate-900/90 transition-all duration-200
                          animate-fade-in`}
              style={{ animationDelay: `${Math.min(i, 8) * 40}ms` }}
            >
              <div className="flex items-start justify-between gap-2 mb-1.5">
                <span className="text-xs font-semibold text-slate-300 truncate flex items-center gap-1.5">
                  {c.author || 'anonymous'}
                </span>
                {typeof c.confidence === 'number' && (
                  <span
                    className={`text-[10px] px-1.5 py-0.5 rounded border ${t.pillBg} ${t.accent} font-bold tabular-nums shrink-0`}
                  >
                    {(c.confidence * 100).toFixed(0)}%
                  </span>
                )}
              </div>
              <p className="text-sm text-slate-200 leading-relaxed break-words whitespace-pre-wrap">
                {c.text}
              </p>
              {typeof c.likeCount === 'number' && c.likeCount > 0 && (
                <div className="mt-2 flex items-center gap-1 text-[11px] text-slate-500">
                  <Heart className="w-3 h-3" />
                  <span className="tabular-nums">{c.likeCount.toLocaleString()}</span>
                </div>
              )}
            </article>
          ))
        )}
      </div>

      {total > expanded && (
        <button
          onClick={() => setExpanded((n) => n + 20)}
          className={`w-full py-3 text-xs font-semibold border-t border-slate-800/60 ${t.accent}
                      hover:bg-slate-900/60 flex items-center justify-center gap-1.5 transition-colors`}
        >
          Show {Math.min(20, total - expanded)} more
          <ChevronDown className="w-3.5 h-3.5" />
        </button>
      )}
    </div>
  );
}
