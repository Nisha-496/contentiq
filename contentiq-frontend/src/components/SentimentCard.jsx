import { useState } from 'react';
import { ChevronDown } from 'lucide-react';

const themes = {
  positive: {
    ring: 'ring-emerald-500/30',
    bg: 'bg-emerald-500/5',
    border: 'border-emerald-500/30',
    accent: 'text-emerald-400',
    pillBg: 'bg-emerald-500/15',
    headerGlow: 'shadow-emerald-900/30',
    badge: 'bg-emerald-500/20 text-emerald-300 border-emerald-500/30',
  },
  negative: {
    ring: 'ring-rose-500/30',
    bg: 'bg-rose-500/5',
    border: 'border-rose-500/30',
    accent: 'text-rose-400',
    pillBg: 'bg-rose-500/15',
    headerGlow: 'shadow-rose-900/30',
    badge: 'bg-rose-500/20 text-rose-300 border-rose-500/30',
  },
  question: {
    ring: 'ring-amber-500/30',
    bg: 'bg-amber-500/5',
    border: 'border-amber-500/30',
    accent: 'text-amber-400',
    pillBg: 'bg-amber-500/15',
    headerGlow: 'shadow-amber-900/30',
    badge: 'bg-amber-500/20 text-amber-300 border-amber-500/30',
  },
};

export default function SentimentCard({ kind, icon, title, comments, emptyMessage }) {
  const t = themes[kind];
  const [expanded, setExpanded] = useState(10);

  const total = comments.length;
  const visible = comments.slice(0, expanded);

  return (
    <div
      className={`glass rounded-2xl border ${t.border} ${t.bg} overflow-hidden animate-slide-up flex flex-col`}
    >
      <div className={`px-5 py-4 border-b border-slate-800/60 shadow-inner ${t.headerGlow}`}>
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <span className={`text-xl ${t.accent}`}>{icon}</span>
            <h3 className="font-semibold text-white tracking-tight">{title}</h3>
          </div>
          <span
            className={`text-xs px-2 py-1 rounded-full border ${t.badge} font-medium tabular-nums`}
          >
            {total}
          </span>
        </div>
      </div>

      <div className="p-3 space-y-2 max-h-[480px] overflow-y-auto flex-1">
        {total === 0 ? (
          <div className="text-center text-slate-500 text-sm py-12">
            {emptyMessage || 'No comments in this category.'}
          </div>
        ) : (
          visible.map((c, i) => (
            <article
              key={c.id || i}
              className="rounded-xl bg-slate-900/60 border border-slate-800/60 p-3 hover:border-slate-700 transition-colors"
            >
              <div className="flex items-start justify-between gap-2 mb-1">
                <span className="text-xs font-medium text-slate-300 truncate">
                  {c.author || 'anonymous'}
                </span>
                {typeof c.confidence === 'number' && (
                  <span
                    className={`text-[10px] px-1.5 py-0.5 rounded ${t.pillBg} ${t.accent} font-medium tabular-nums shrink-0`}
                  >
                    {(c.confidence * 100).toFixed(0)}%
                  </span>
                )}
              </div>
              <p className="text-sm text-slate-300 leading-relaxed break-words whitespace-pre-wrap">
                {c.text}
              </p>
              {typeof c.likeCount === 'number' && c.likeCount > 0 && (
                <div className="mt-2 text-[11px] text-slate-500">
                  {c.likeCount.toLocaleString()} like{c.likeCount === 1 ? '' : 's'}
                </div>
              )}
            </article>
          ))
        )}
      </div>

      {total > expanded && (
        <button
          onClick={() => setExpanded((n) => n + 20)}
          className={`w-full py-3 text-xs font-medium border-t border-slate-800/60 ${t.accent} hover:bg-slate-900/40 flex items-center justify-center gap-1.5 transition-colors`}
        >
          Show {Math.min(20, total - expanded)} more
          <ChevronDown className="w-3.5 h-3.5" />
        </button>
      )}
    </div>
  );
}
