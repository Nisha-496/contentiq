import { useEffect, useState } from 'react';
import { Loader2, Youtube, AlertCircle, CheckCircle2, Clock } from 'lucide-react';
import { videos as videosApi, analysis as analysisApi } from '../api/client';

const statusIcon = (status) => {
  switch ((status || '').toUpperCase()) {
    case 'COMPLETED':
      return <CheckCircle2 className="w-4 h-4 text-emerald-400" />;
    case 'IN_PROGRESS':
      return <Clock className="w-4 h-4 text-amber-400 animate-pulse" />;
    case 'FAILED':
      return <AlertCircle className="w-4 h-4 text-rose-400" />;
    default:
      return <Clock className="w-4 h-4 text-slate-400" />;
  }
};

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
      <div className="glass rounded-2xl p-8 text-center">
        <AlertCircle className="w-8 h-8 text-rose-400 mx-auto mb-2" />
        <p className="text-slate-300">{error}</p>
      </div>
    );
  }

  if (items.length === 0) {
    return (
      <div className="glass rounded-2xl p-12 text-center">
        <Youtube className="w-12 h-12 text-slate-600 mx-auto mb-4" />
        <h3 className="text-lg font-semibold text-white">No videos yet</h3>
        <p className="text-sm text-slate-400 mt-1">
          Head to the Dashboard and analyze a YouTube video to populate your history.
        </p>
      </div>
    );
  }

  return (
    <div className="space-y-3 animate-fade-in">
      <div className="flex items-center justify-between mb-2">
        <h2 className="text-2xl font-bold text-white tracking-tight">History</h2>
        <span className="text-sm text-slate-500">
          {items.length} video{items.length === 1 ? '' : 's'}
        </span>
      </div>

      <div className="space-y-2">
        {items.map(({ video, latest, total }) => (
          <article
            key={video.id}
            className="glass rounded-xl p-4 hover:border-slate-700 transition-colors flex items-center gap-4 animate-slide-up"
          >
            <div className="w-10 h-10 rounded-lg bg-rose-500/10 flex items-center justify-center shrink-0">
              <Youtube className="w-5 h-5 text-rose-400" />
            </div>

            <div className="flex-1 min-w-0">
              <a
                href={video.url}
                target="_blank"
                rel="noreferrer"
                className="font-medium text-white hover:text-brand-300 line-clamp-1 transition-colors"
              >
                {video.title || 'Untitled video'}
              </a>
              <div className="text-xs text-slate-500 mt-0.5">
                {video.channelName || 'unknown'}
                {video.createdAt && (
                  <>
                    {' '}
                    &middot;{' '}
                    {new Date(video.createdAt).toLocaleString(undefined, {
                      dateStyle: 'medium',
                      timeStyle: 'short',
                    })}
                  </>
                )}
              </div>
            </div>

            <div className="hidden md:flex items-center gap-6 text-xs">
              {latest ? (
                <>
                  <Stat
                    label="Positive"
                    value={latest.positiveCount}
                    color="text-emerald-400"
                  />
                  <Stat
                    label="Negative"
                    value={latest.negativeCount}
                    color="text-rose-400"
                  />
                  <Stat
                    label="Neutral"
                    value={latest.neutralCount}
                    color="text-slate-400"
                  />
                </>
              ) : (
                <span className="text-slate-500 italic">no analysis yet</span>
              )}
              <div className="flex items-center gap-1.5 text-slate-400">
                {statusIcon(latest?.status)}
                <span>{latest?.status || '—'}</span>
              </div>
              <span className="text-slate-500">
                {total} report{total === 1 ? '' : 's'}
              </span>
            </div>
          </article>
        ))}
      </div>
    </div>
  );
}

function Stat({ label, value, color }) {
  return (
    <div className="text-center">
      <div className={`font-bold tabular-nums ${color}`}>{value ?? 0}</div>
      <div className="text-[10px] uppercase tracking-wider text-slate-500">{label}</div>
    </div>
  );
}
