import { useState } from 'react';
import toast from 'react-hot-toast';
import {
  Youtube, Sparkles, Loader2, ThumbsUp, ThumbsDown, HelpCircle, Play,
  ArrowRight, Activity, Brain, Zap,
} from 'lucide-react';
import { videos as videosApi, comments as commentsApi, analysis as analysisApi } from '../api/client';
import SummaryBar from '../components/SummaryBar';
import SentimentCard from '../components/SentimentCard';

const POLL_INTERVAL_MS = 2000;
const POLL_MAX_ATTEMPTS = 60;
const QUESTION_HINTS = ['how', 'why', 'what', 'when', 'where', 'who', 'which'];

function isQuestion(text) {
  if (!text) return false;
  const trimmed = text.trim();
  if (trimmed.endsWith('?')) return true;
  const lower = trimmed.toLowerCase();
  return QUESTION_HINTS.some((hint) => lower.startsWith(hint + ' '));
}

function categorize(comments) {
  const positive = [], negative = [], neutral = [], questions = [];
  for (const c of comments) {
    if (isQuestion(c.text)) { questions.push(c); continue; }
    const s = (c.sentiment || 'NEUTRAL').toUpperCase();
    if (s === 'POSITIVE') positive.push(c);
    else if (s === 'NEGATIVE') negative.push(c);
    else neutral.push(c);
  }
  const sortKey = (c) => -((c.confidence || 0) * 1000 + Math.log10((c.likeCount || 0) + 1));
  positive.sort((a, b) => sortKey(a) - sortKey(b));
  negative.sort((a, b) => sortKey(a) - sortKey(b));
  neutral.sort((a, b) => sortKey(a) - sortKey(b));
  questions.sort((a, b) => -((a.likeCount || 0) - (b.likeCount || 0)));
  return { positive, negative, neutral, questions };
}

function videoIdFromUrl(url) {
  try {
    const u = new URL(url);
    if (u.hostname.includes('youtu.be')) return u.pathname.slice(1);
    return u.searchParams.get('v') || null;
  } catch { return null; }
}

const sleep = (ms) => new Promise((r) => setTimeout(r, ms));

export default function Dashboard() {
  const [url, setUrl] = useState('');
  const [busy, setBusy] = useState(false);
  const [stage, setStage] = useState('');
  const [video, setVideo] = useState(null);
  const [report, setReport] = useState(null);
  const [buckets, setBuckets] = useState(null);

  const reset = () => { setVideo(null); setReport(null); setBuckets(null); };

  const analyze = async (e) => {
    e.preventDefault();
    if (!url.trim()) return;
    reset();
    setBusy(true);
    try {
      setStage('Importing video from YouTube...');
      const importedVideo = await videosApi.importFromYouTube(url.trim(), null);
      setVideo(importedVideo);

      setStage(`Fetching comments for "${importedVideo.title}"...`);
      const importResult = await commentsApi.importFromYouTube(importedVideo.id, 100);
      const importedCount = importResult.imported ?? 0;

      if (importedCount === 0) {
        toast.error('No comments found for this video.');
        return;
      }

      setStage(`Analyzing ${importedCount} comments with HuggingFace...`);
      const initialReport = await analysisApi.analyzeComments(importedVideo.id);

      let finalReport = initialReport;
      if (finalReport.status === 'IN_PROGRESS') {
        for (let i = 0; i < POLL_MAX_ATTEMPTS; i++) {
          await sleep(POLL_INTERVAL_MS);
          finalReport = await analysisApi.getReport(finalReport.id);
          if (finalReport.status !== 'IN_PROGRESS') break;
        }
      }
      setReport(finalReport);

      setStage('Loading classified comments...');
      const allComments = await commentsApi.forVideo(importedVideo.id);
      setBuckets(categorize(allComments));

      if (finalReport.status === 'COMPLETED') {
        toast.success('Analysis complete!');
      } else {
        toast.error(`Analysis ${finalReport.status}: ${finalReport.errorMessage || ''}`);
      }
    } catch (err) {
      const msg = err.response?.data?.message || err.response?.data?.error || err.message || 'Analysis failed';
      toast.error(msg);
    } finally {
      setBusy(false);
      setStage('');
    }
  };

  const summary = report?.summary;
  const total = buckets ? buckets.positive.length + buckets.negative.length + buckets.neutral.length + buckets.questions.length : 0;
  const ytId = video ? videoIdFromUrl(video.url) : null;

  return (
    <div className="space-y-6">
      {!buckets && !busy && (
        <section className="relative overflow-hidden rounded-3xl py-12 px-6 md:py-16 md:px-12 animate-fade-in">
          <div className="absolute inset-0 bg-hero-glow pointer-events-none" />
          <div className="absolute inset-0 bg-[radial-gradient(circle_at_30%_50%,rgba(168,85,247,0.08),transparent_50%)] pointer-events-none" />

          <div className="relative max-w-3xl">
            <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full
                            bg-brand-500/10 border border-brand-500/30 text-brand-300
                            text-xs font-semibold uppercase tracking-wider mb-5 animate-slide-down">
              <Sparkles className="w-3.5 h-3.5" />
              AI-powered
            </div>
            <h1 className="text-4xl md:text-5xl font-extrabold tracking-tight leading-[1.05] text-balance mb-4 animate-slide-up">
              Decode any YouTube video's <span className="brand-text">audience sentiment</span>.
            </h1>
            <p className="text-lg text-slate-400 max-w-2xl text-pretty animate-slide-up stagger-2">
              Paste a video URL — we'll pull the comments, classify each one's emotion with a transformer model,
              and surface what your audience really thinks.
            </p>

            <div className="flex flex-wrap gap-4 mt-6 text-xs text-slate-500 animate-slide-up stagger-3">
              <Feature icon={<Brain className="w-3.5 h-3.5" />} label="HuggingFace transformer" />
              <Feature icon={<Zap className="w-3.5 h-3.5" />} label="Batch analysis under 5s" />
              <Feature icon={<Activity className="w-3.5 h-3.5" />} label="Live polling" />
            </div>
          </div>
        </section>
      )}

      <section className="glass-strong rounded-2xl p-6 animate-slide-up relative overflow-hidden">
        <div className="absolute -top-20 -right-20 w-48 h-48 bg-brand-500/10 rounded-full blur-3xl pointer-events-none" />

        <div className="flex items-center gap-2 mb-4 relative">
          <div className="w-8 h-8 rounded-lg bg-brand-500/15 flex items-center justify-center">
            <Youtube className="w-4 h-4 text-rose-400" />
          </div>
          <h2 className="text-base font-semibold text-white tracking-tight">Analyze a YouTube video</h2>
        </div>

        <form onSubmit={analyze} className="flex flex-col sm:flex-row gap-3 relative">
          <input
            type="text"
            value={url}
            onChange={(e) => setUrl(e.target.value)}
            placeholder="https://www.youtube.com/watch?v=..."
            className="input flex-1"
            disabled={busy}
            required
          />
          <button type="submit" disabled={busy || !url.trim()} className="btn-primary px-6 sm:min-w-[160px]">
            {busy ? (
              <>
                <Loader2 className="w-4 h-4 animate-spin" />
                Analyzing
              </>
            ) : (
              <>
                <Play className="w-4 h-4" />
                Analyze
                <ArrowRight className="w-4 h-4 opacity-70" />
              </>
            )}
          </button>
        </form>

        {busy && stage && (
          <div className="mt-4 flex items-center gap-3 px-4 py-3 rounded-lg bg-slate-900/60 border border-slate-800/60 relative animate-fade-in">
            <Loader2 className="w-4 h-4 animate-spin text-brand-400 shrink-0" />
            <span className="text-sm text-slate-300">{stage}</span>
            <div className="ml-auto h-1 w-32 rounded-full bg-slate-800 overflow-hidden">
              <div className="h-full w-1/3 bg-gradient-to-r from-transparent via-brand-400 to-transparent animate-shimmer" />
            </div>
          </div>
        )}
      </section>

      {video && (
        <section className="glass rounded-2xl p-5 flex items-center gap-4 animate-slide-up">
          {ytId ? (
            <a href={video.url} target="_blank" rel="noreferrer" className="block shrink-0 group">
              <div className="relative w-32 h-20 rounded-xl overflow-hidden bg-slate-900 border border-slate-800
                              transition-transform duration-300 group-hover:scale-105">
                <img
                  src={`https://i.ytimg.com/vi/${ytId}/mqdefault.jpg`}
                  alt={video.title}
                  className="w-full h-full object-cover"
                  loading="lazy"
                />
                <div className="absolute inset-0 bg-gradient-to-tr from-black/40 to-transparent flex items-center justify-center
                                opacity-0 group-hover:opacity-100 transition-opacity">
                  <Play className="w-7 h-7 text-white fill-white drop-shadow-lg" />
                </div>
              </div>
            </a>
          ) : (
            <div className="w-12 h-12 rounded-lg bg-rose-500/10 flex items-center justify-center shrink-0">
              <Youtube className="w-6 h-6 text-rose-400" />
            </div>
          )}
          <div className="min-w-0 flex-1">
            <a
              href={video.url}
              target="_blank"
              rel="noreferrer"
              className="block text-lg font-bold text-white hover:text-brand-300 line-clamp-2 transition-colors leading-snug"
            >
              {video.title}
            </a>
            <p className="text-sm text-slate-400 mt-1">
              by <span className="text-slate-200 font-medium">{video.channelName || 'unknown'}</span>
              {video.durationSeconds != null && (
                <span className="text-slate-500"> · {formatDuration(video.durationSeconds)}</span>
              )}
            </p>
          </div>
        </section>
      )}

      {buckets && (
        <>
          <SummaryBar
            total={total}
            positive={buckets.positive.length}
            negative={buckets.negative.length}
            questions={buckets.questions.length}
            neutralCount={buckets.neutral.length}
          />

          {summary && (
            <section className="glass rounded-2xl p-5 animate-slide-up relative overflow-hidden">
              <div className="absolute top-0 left-0 w-full h-px bg-gradient-to-r from-transparent via-brand-500/40 to-transparent" />
              <div className="flex items-center gap-2 mb-3">
                <Sparkles className="w-4 h-4 text-brand-400" />
                <h3 className="text-xs font-semibold text-slate-400 uppercase tracking-[0.18em]">
                  Insight summary
                </h3>
              </div>
              <p className="text-slate-200 leading-relaxed text-[15px] text-pretty">{summary}</p>
            </section>
          )}

          <div className="grid grid-cols-1 lg:grid-cols-3 gap-5">
            <div className="animate-slide-up stagger-1">
              <SentimentCard
                kind="positive"
                icon={<ThumbsUp className="w-5 h-5" />}
                title="Positive"
                comments={buckets.positive}
                emptyMessage="No positive comments detected."
              />
            </div>
            <div className="animate-slide-up stagger-2">
              <SentimentCard
                kind="negative"
                icon={<ThumbsDown className="w-5 h-5" />}
                title="Negative"
                comments={buckets.negative}
                emptyMessage="No negative comments detected."
              />
            </div>
            <div className="animate-slide-up stagger-3">
              <SentimentCard
                kind="question"
                icon={<HelpCircle className="w-5 h-5" />}
                title="Questions"
                comments={buckets.questions}
                emptyMessage="No questions detected."
              />
            </div>
          </div>

          {buckets.neutral.length > 0 && (
            <details className="glass rounded-2xl p-4 animate-fade-in group">
              <summary className="cursor-pointer text-sm font-medium text-slate-300 hover:text-white transition-colors
                                  flex items-center gap-2 list-none">
                <ChevronRight className="w-4 h-4 transition-transform group-open:rotate-90" />
                Show {buckets.neutral.length} neutral comment{buckets.neutral.length === 1 ? '' : 's'}
              </summary>
              <div className="mt-4 grid grid-cols-1 md:grid-cols-2 gap-2 max-h-96 overflow-y-auto">
                {buckets.neutral.slice(0, 50).map((c, i) => (
                  <div key={c.id || i} className="rounded-lg bg-slate-900/60 border border-slate-800/60 p-3 text-sm text-slate-300">
                    <div className="text-xs text-slate-500 mb-1">{c.author || 'anonymous'}</div>
                    <div className="break-words">{c.text}</div>
                  </div>
                ))}
              </div>
            </details>
          )}
        </>
      )}
    </div>
  );
}

function Feature({ icon, label }) {
  return (
    <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full
                     bg-slate-900/60 border border-slate-800/60">
      <span className="text-brand-400">{icon}</span>
      <span>{label}</span>
    </span>
  );
}

function ChevronRight(props) {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"
         strokeLinecap="round" strokeLinejoin="round" {...props}>
      <polyline points="9 18 15 12 9 6" />
    </svg>
  );
}

function formatDuration(secs) {
  const m = Math.floor(secs / 60);
  const s = secs % 60;
  if (m < 60) return `${m}:${s.toString().padStart(2, '0')}`;
  const h = Math.floor(m / 60);
  return `${h}:${(m % 60).toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`;
}
