import { useEffect, useState } from 'react';
import {
  Loader2, Youtube, AlertCircle, CheckCircle2, Clock, History as HistoryIcon, Play,
} from 'lucide-react';
import { videos as videosApi, analysis as analysisApi } from '../api/client';

const statusBadge = (status) => {
  const s = (status || '').toUpperCase();
  if (s === 'COMPLETED') {
    return {
      icon: <CheckCircle2 className="w-3 h-3" />,
      label: 'Completed',
      cls: 'bg-emerald-500/15 text-emerald-300 border-emerald-500/30',
    };
  }
  if (s === 'IN_PROGRESS') {
    return {
      icon: <Clock className="w-3 h-3 animate-pulse" />,
      label: 'In progress',
      cls: 'bg-amber-500/15 text-amber-300 border-amber-500/30',
    };
  }
  if (s === 'FAILED') {
    return {
      icon: <AlertCircle className="w-3 h-3" />,
      label: 'Failed',
      cls: 'bg-rose-500/15 text-rose-300 border-rose-500/30',
    };
  }
  return {
    icon: <Clock className="w-3 h-3" />,
    label: 'No report',
    cls: 'bg-slate-700/40 text-slate-400 border-slate-600/40',
  };
};

function videoIdFromUrl(url) {
  try {
    const u = new URL(url);
    if (u.hostname.includes('youtu.be')) return u.pathname.slice(1);
    return u.searchParams.get('v') || null;
  } catch {
    return null;
  }
}

export default function History() {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [items, setItems] = useState([]);

  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        const videos = await videosApi.list();
        const enriched = await Promise.all(
          videos.map(async (v) => {
            try {
              const reports = await analysisApi.reportsForVideo(v.id);
              const sorted = (reports || []).sort((a, b) => {
                const ta = new Date(a.createdAt || 0).getTime();
                const tb = new Date(b.createdAt || 0).getTime();
                return tb - ta;
              });
              return { video: v, latest: sorted[0] || null, total: sorted.length };
            } catch {
              return { video: v, latest: null, total: 0 };
            }
          })
        );
        if (!cancelled) {
          enriched.sort((a, b) => {
            const ta = new Date(a.video.createdAt || 0).getTime();
            const tb = new Date(b.video.createdAt || 0).getTime();
            return tb - ta;
          });
          setItems(enriched);
        }
      } catch (err) {
        if (!cancelled) setError(err.response?.data?.message || err.message);
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, []);

  if (loading) {
    return (
      <div className="flex items-center justify-center py-20 text-slate-400">
        <Loader2 className="w-5 h-5 animate-spin mr-2" />
        Loading history...
      </div>
    );
  }

  if (error) {
    return (
      <div className="glass-strong rounded-2xl p-8 text-center max-w-md mx-auto">
        <AlertCircle className="w-10 h-10 text-rose-400 mx-auto mb-3" />
        <h3 className="text-lg font-semibold text-white mb-1">Couldn't load history</h3>
        <p className="text-sm text-slate-400">{error}</p>
      </div>
    );
  }

  if (items.length === 0) {
    return (
      <div className="glass-strong rounded-2xl p-16 text-center max-w-md mx-auto animate-fade-in">
        <div className="w-16 h-16 rounded-2xl bg-slate-800/60 flex items-center justify-center mx-auto mb-4">
          <HistoryIcon className="w-8 h-8 text-slate-500" />
        </div>
        <h3 className="text-lg font-bold text-white">No videos yet</h3>
        <p className="text-sm text-slate-400 mt-1">
          Head to the Dashboard and analyze a YouTube video to populate your history.
        </p>
      </div>
    );
  }

  return (
    <div className="animate-fade-in space-y-5">
      <div className="flex items-end justify-between mb-1">
        <div>
          <div className="flex items-center gap-2 mb-1">
            <HistoryIcon className="w-4 h-4 text-brand-400" />
            <span className="text-xs uppercase tracking-[0.18em] font-semibold text-slate-400">
              Past analyses
            </span>
          </div>
          <h2 className="text-3xl font-bold text-white tracking-tight">History</h2>
        </div>
        <span className="text-sm text-slate-500">
          {items.length} video{items.length === 1 ? '' : 's'}
        </span>
      </div>

      <div className="grid grid-cols-1 gap-3">
        {items.map(({ video, latest, total }, i) => {
          const ytId = videoIdFromUrl(video.url);
          const badge = statusBadge(latest?.status);
          return (
            <article
              key={video.id}
              className="glass rounded-2xl p-4 hover:border-slate-700 transition-all duration-200
                         hover:-translate-y-0.5 flex items-center gap-4 animate-slide-up"
              style={{ animationDelay: `${Math.min(i, 8) * 60}ms` }}
            >
              {ytId ? (
                <a href={video.url} target="_blank" rel="noreferrer" className="block shrink-0 group">
                  <div className="relative w-24 h-16 rounded-lg overflow-hidden bg-slate-900 border border-slate-800
                                  transition-transform duration-300 group-hover:scale-105">
                    <img
                      src={`https://i.ytimg.com/vi/${ytId}/mqdefault.jpg`}
                      alt=""
                      className="w-full h-full object-cover"
                      loading="lazy"
                    />
                    <div className="absolute inset-0 bg-black/30 opacity-0 group-hover:opacity-100 transition-opacity
                                    flex items-center justify-center">
                      <Play className="w-5 h-5 text-white fill-white" />
                    </div>
                  </div>
                </a>
              ) : (
                <div className="w-12 h-12 rounded-lg bg-rose-500/10 flex items-center justify-center shrink-0">
                  <Youtube className="w-5 h-5 text-rose-400" />
                </div>
              )}

              <div className="flex-1 min-w-0">
                <a
                  href={video.url}
                  target="_blank"
                  rel="noreferrer"
                  className="font-semibold text-white hover:text-brand-300 line-clamp-1 transition-colors"
                >
                  {video.title || 'Untitled video'}
                </a>
                <div className="text-xs text-slate-500 mt-1 flex items-center gap-2">
                  <span className="text-slate-400">{video.channelName || 'unknown'}</span>
                  {video.createdAt && (
                    <>
                      <span className="text-slate-700">·</span>
                      <span>
                        {new Date(video.createdAt).toLocaleString(undefined, {
                          dateStyle: 'medium',
                          timeStyle: 'short',
                        })}
                      </span>
                    </>
                  )}
                  <span className="text-slate-700">·</span>
                  <span>
                    {total} report{total === 1 ? '' : 's'}
                  </span>
                </div>

                {latest && latest.totalItems > 0 && (
                  <SentimentMini
                    total={latest.totalItems}
                    positive={latest.positiveCount || 0}
                    neutral={latest.neutralCount || 0}
                    negative={latest.negativeCount || 0}
                  />
                )}
              </div>

              <div className="hidden md:flex items-center gap-5 text-xs shrink-0">
                {latest && (
                  <div className="flex items-center gap-4">
                    <Stat label="POS" value={latest.positiveCount} color="text-emerald-300" />
                    <Stat label="NEU" value={latest.neutralCount} color="text-slate-300" />
                    <Stat label="NEG" value={latest.negativeCount} color="text-rose-300" />
                  </div>
                )}
                <span
                  className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full border text-[11px] font-semibold ${badge.cls}`}
                >
                  {badge.icon}
                  {badge.label}
                </span>
              </div>
            </article>
          );
        })}
      </div>
    </div>
  );
}

function SentimentMini({ total, positive, neutral, negative }) {
  if (!total) return null;
  const pct = (n) => (100 * n) / total;
  return (
    <div className="mt-2 h-1.5 rounded-full bg-slate-800/60 overflow-hidden flex max-w-md">
      <div
        className="h-full bg-gradient-to-r from-emerald-400 to-emerald-500"
        style={{ width: `${pct(positive)}%` }}
      />
      <div
        className="h-full bg-gradient-to-r from-slate-600 to-slate-700"
        style={{ width: `${pct(neutral)}%` }}
      />
      <div
        className="h-full bg-gradient-to-r from-rose-400 to-rose-500"
        style={{ width: `${pct(negative)}%` }}
      />
    </div>
  );
}

function Stat({ label, value, color }) {
  return (
    <div className="text-center min-w-[36px]">
      <div className={`text-base font-bold tabular-nums ${color}`}>{value ?? 0}</div>
      <div className="text-[9px] uppercase tracking-wider text-slate-600 font-semibold">{label}</div>
    </div>
  );
}
