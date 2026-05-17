import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { Youtube, Sparkles, Loader2, ThumbsUp, ThumbsDown, HelpCircle, Play } from 'lucide-react';
import { videos as videosApi, comments as commentsApi, analysis as analysisApi } from '../api/client';
import SummaryBar from '../components/SummaryBar';
import SentimentCard from '../components/SentimentCard';

const POLL_INTERVAL_MS = 2000;
const POLL_MAX_ATTEMPTS = 60; // 2 minutes max
const QUESTION_HINTS = ['how', 'why', 'what', 'when', 'where', 'who', 'which'];

function isQuestion(text) {
  if (!text) return false;
  const trimmed = text.trim();
  if (trimmed.endsWith('?')) return true;
  const lower = trimmed.toLowerCase();
  return QUESTION_HINTS.some((hint) => lower.startsWith(hint + ' '));
}

function categorize(comments) {
  const positive = [];
  const negative = [];
  const neutral = [];
  const questions = [];
  for (const c of comments) {
    if (isQuestion(c.text)) {
      questions.push(c);
      continue;
    }
    const s = (c.sentiment || 'NEUTRAL').toUpperCase();
    if (s === 'POSITIVE') positive.push(c);
    else if (s === 'NEGATIVE') negative.push(c);
    else neutral.push(c);
  }
  // sort each by confidence then likes desc
  const sortKey = (c) => -((c.confidence || 0) * 1000 + Math.log10((c.likeCount || 0) + 1));
  positive.sort((a, b) => sortKey(a) - sortKey(b));
  negative.sort((a, b) => sortKey(a) - sortKey(b));
  neutral.sort((a, b) => sortKey(a) - sortKey(b));
  questions.sort((a, b) => -((a.likeCount || 0) - (b.likeCount || 0)));
  return { positive, negative, neutral, questions };
}

function sleep(ms) {
  return new Promise((r) => setTimeout(r, ms));
}

export default function Dashboard() {
  const navigate = useNavigate();
  const [url, setUrl] = useState('');
  const [busy, setBusy] = useState(false);
  const [stage, setStage] = useState('');
  const [video, setVideo] = useState(null);
  const [report, setReport] = useState(null);
  const [buckets, setBuckets] = useState(null);

  const reset = () => {
    setVideo(null);
    setReport(null);
    setBuckets(null);
  };

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
      const msg =
        err.response?.data?.message ||
        err.response?.data?.error ||
        err.message ||
        'Analysis failed';
      toast.error(msg);
    } finally {
      setBusy(false);
      setStage('');
    }
  };

  const summary = report?.summary;
  const total = buckets ? buckets.positive.length + buckets.negative.length + buckets.neutral.length + buckets.questions.length : 0;

  return (
    <div className="space-y-6">
      <section className="glass rounded-2xl p-6 animate-fade-in">
        <div className="flex items-center gap-2 mb-1">
          <Sparkles className="w-5 h-5 text-brand-400" />
          <h2 className="text-xl font-bold text-white tracking-tight">Analyze a YouTube video</h2>
        </div>
        <p className="text-sm text-slate-400 mb-4">
          Paste any YouTube URL. We&apos;ll fetch the comments and classify their sentiment.
        </p>

        <form onSubmit={analyze} className="flex flex-col sm:flex-row gap-3">
          <div className="relative flex-1">
            <Youtube className="w-4 h-4 text-rose-500 absolute left-3 top-1/2 -translate-y-1/2 pointer-events-none" />
            <input
              type="text"
              value={url}
              onChange={(e) => setUrl(e.target.value)}
              placeholder="https://www.youtube.com/watch?v=..."
              className="input pl-9"
              disabled={busy}
              required
            />
          </div>
          <button type="submit" disabled={busy || !url.trim()} className="btn-primary px-6">
            {busy ? (
              <>
                <Loader2 className="w-4 h-4 animate-spin" />
                Analyzing...
              </>
            ) : (
              <>
                <Play className="w-4 h-4" />
                Analyze
              </>
            )}
          </button>
        </form>

        {busy && stage && (
          <div className="mt-4 flex items-center gap-2 text-sm text-slate-300 animate-fade-in">
            <Loader2 className="w-4 h-4 animate-spin text-brand-400" />
            <span>{stage}</span>
          </div>
        )}
      </section>

      {video && (
        <section className="glass rounded-2xl p-5 flex items-start gap-4 animate-slide-up">
          <div className="w-12 h-12 rounded-lg bg-rose-500/10 flex items-center justify-center shrink-0">
            <Youtube className="w-6 h-6 text-rose-400" />
          </div>
          <div className="min-w-0 flex-1">
            <a
              href={video.url}
              target="_blank"
              rel="noreferrer"
              className="text-lg font-semibold text-white hover:text-brand-300 line-clamp-1 transition-colors"
            >
              {video.title}
            </a>
            <p className="text-sm text-slate-400">
              by <span className="text-slate-300">{video.channelName || 'unknown'}</span>
              {video.durationSeconds && (
                <span className="text-slate-500"> &middot; {video.durationSeconds}s</span>
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
            <section className="glass rounded-2xl p-5 animate-slide-up">
              <h3 className="text-sm font-medium text-slate-400 uppercase tracking-wider mb-2">
                Summary
              </h3>
              <p className="text-slate-200 leading-relaxed text-[15px]">{summary}</p>
            </section>
          )}

          <div className="grid grid-cols-1 lg:grid-cols-3 gap-5">
            <SentimentCard
              kind="positive"
              icon={<ThumbsUp className="w-5 h-5" />}
              title="Positive"
              comments={buckets.positive}
              emptyMessage="No positive comments detected."
            />
            <SentimentCard
              kind="negative"
              icon={<ThumbsDown className="w-5 h-5" />}
              title="Negative"
              comments={buckets.negative}
              emptyMessage="No negative comments detected."
            />
            <SentimentCard
              kind="question"
              icon={<HelpCircle className="w-5 h-5" />}
              title="Questions"
              comments={buckets.questions}
              emptyMessage="No questions detected."
            />
          </div>

          {buckets.neutral.length > 0 && (
            <details className="glass rounded-2xl p-4 animate-fade-in">
              <summary className="cursor-pointer text-sm font-medium text-slate-300 hover:text-white transition-colors">
                Show {buckets.neutral.length} neutral comment{buckets.neutral.length === 1 ? '' : 's'}
              </summary>
              <div className="mt-4 grid grid-cols-1 md:grid-cols-2 gap-2 max-h-96 overflow-y-auto">
                {buckets.neutral.slice(0, 50).map((c, i) => (
                  <div
                    key={c.id || i}
                    className="rounded-lg bg-slate-900/60 border border-slate-800/60 p-3 text-sm text-slate-300"
                  >
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
