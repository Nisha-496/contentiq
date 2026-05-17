# ContentIQ

> AI-powered YouTube Content Intelligence Platform — fetch any video's comments, classify their sentiment with HuggingFace, and turn transcripts into structured PDF notes with Claude.

[![CI](https://github.com/Nisha-496/contentiq/actions/workflows/ci.yml/badge.svg)](https://github.com/Nisha-496/contentiq/actions/workflows/ci.yml)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.14-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18-61dafb.svg)](https://react.dev/)
[![Vite](https://img.shields.io/badge/Vite-6-646cff.svg)](https://vitejs.dev/)
[![Tailwind](https://img.shields.io/badge/Tailwind-3-06b6d4.svg)](https://tailwindcss.com/)
[![MongoDB](https://img.shields.io/badge/MongoDB-7-47A248.svg)](https://www.mongodb.com/)
[![HuggingFace](https://img.shields.io/badge/HuggingFace-Inference-ffd21e.svg)](https://huggingface.co/cardiffnlp/twitter-roberta-base-sentiment-latest)

---

## What it does

Paste a YouTube URL → ContentIQ pulls the video's metadata + top comments → runs each comment through a transformer-based sentiment classifier → renders a dashboard of **POSITIVE / NEGATIVE / QUESTIONS** with confidence scores, generates a deterministic summary, and emits a PDF report. Transcripts can additionally be fed to Claude to produce structured study notes.

### Core features
- **YouTube import** — bring in video metadata and up to 500 comments per video via the Data API v3
- **Sentiment classification** — `cardiffnlp/twitter-roberta-base-sentiment-latest` via the HuggingFace Inference Router (batched)
- **Question detection** — client-side filter on the React app surfaces comments ending in `?` (or starting with how / why / what / when / where / who / which) into a dedicated card
- **Deterministic analytics** — counts, percentages, top-liked comments per sentiment, stacked progress bar
- **PDF reports** — iText-rendered sentiment reports + notes documents land in `generated-pdfs/`
- **JWT auth** — stateless Spring Security with BCrypt-hashed passwords, persisted to MongoDB
- **Resilient AI calls** — decorator chain wraps the Claude HTTP client with retry + rate-limit + logging
- **Background recovery** — scheduler reruns reports stuck in `IN_PROGRESS` for >10 min

---

## Tech stack

| Layer | Tech |
|---|---|
| Backend | Java 21, Spring Boot 3.5.14, Spring Web MVC + WebFlux (WebClient), Spring Data MongoDB, Spring Security |
| Persistence | MongoDB 7 on `localhost:27017`, database `contentiq_db` |
| Auth | JWT (`io.jsonwebtoken:jjwt 0.12.6`), BCrypt passwords, stateless sessions |
| AI / ML | **HuggingFace** Inference Router (`cardiffnlp/twitter-roberta-base-sentiment-latest`) for sentiment; **Claude** (`claude-sonnet-4-20250514`) for notes generation |
| Third-party | YouTube Data API v3 (`google-api-services-youtube v3-rev20240514-2.0.0`) |
| Reports | iText 8.0.5 PDF |
| Frontend | React 18, Vite 6, Tailwind CSS 3, axios, react-router-dom, lucide-react, react-hot-toast |
| Build | Maven (backend), npm (frontend) |

---

## Design patterns

Seven Gang-of-Four patterns wired through the backend, each chosen because it earns its keep:

| Pattern | Where | Why |
|---|---|---|
| **Builder** | All 6 `@Document` models via Lombok `@Builder` | Immutable-ish construction, named arguments |
| **Strategy** | `AnalysisStrategy<I,O>` with `SentimentAnalysisStrategy`, `CommentSummaryStrategy`, `NotesGenerationStrategy` | Swap algorithms without touching callers |
| **Factory** | `ContentProcessorFactory` selects a strategy via a `ProcessorType` enum | Single place to wire strategy choice |
| **Decorator** | `AIService` chain: `CoreAIService` → `RetryDecorator` → `RateLimitDecorator` → `LoggingDecorator` | Cross-cutting concerns without subclassing |
| **Observer** | `AnalysisEventPublisher` fans out completion to `PDFGenerationObserver` + `NotificationObserver` | Side effects decoupled from core analysis |
| **Singleton** | `AIService` + `YouTube` clients registered as `@Scope("singleton")` beans | One pooled HTTP client per provider |
| **Template Method** | Abstract `NotesGeneratorService` with `produce(Video)` hook, implemented by `AINotesGeneratorService` | Fixed pipeline + variable step |

---

## Architecture

```
┌───────────────┐         ┌────────────────────────────────────────┐
│  React UI     │  JWT    │             Spring Boot API            │
│  :5173        │ ◀──────▶│                :8081                   │
└───────────────┘         │                                        │
                          │  Controllers ── Services ── Strategies │
                          │                    │                   │
                          │                    ▼                   │
                          │     ┌──────────────────────────┐       │
                          │     │ AIService decorator chain│       │
                          │     │ Logging→Limit→Retry→Core │       │
                          │     └──────────────────────────┘       │
                          │                    │                   │
                          │      ┌─────────────┴────────────┐      │
                          │      ▼              ▼           ▼      │
                          │ ┌─────────┐   ┌─────────┐   ┌──────┐   │
                          │ │HuggingFa│   │ Claude  │   │YouTbe│   │
                          │ │  API    │   │   API   │   │ API  │   │
                          │ └─────────┘   └─────────┘   └──────┘   │
                          │                                        │
                          │  Observers ── PDF + Notification       │
                          │       │                                │
                          │       ▼                                │
                          │   MongoDB (contentiq_db)               │
                          └────────────────────────────────────────┘
```

---

## API reference

### Auth (public)
| Method | Path | Body |
|---|---|---|
| `POST` | `/api/auth/register` | `{username, email, password}` → `{token, userId, ...}` |
| `POST` | `/api/auth/login` | `{username, password}` → `{token, userId, ...}` |

### Videos (Bearer)
| Method | Path |
|---|---|
| `GET POST PUT DELETE` | `/api/videos[/{id}]` |

### Comments (Bearer)
| Method | Path |
|---|---|
| `POST` | `/api/comments/bulk` |
| `GET` | `/api/comments/{videoId}` |

### YouTube (Bearer)
| Method | Path | Description |
|---|---|---|
| `GET` | `/api/youtube/metadata?urlOrId=...` | Preview metadata without saving |
| `POST` | `/api/youtube/videos/import` | Import a video |
| `POST` | `/api/youtube/videos/{videoId}/comments/import?maxResults=N` | Fetch + save N comments |

### Analysis (Bearer)
| Method | Path | Description |
|---|---|---|
| `POST` | `/api/analysis/comments/{videoId}` | Run HF sentiment + deterministic summary → fires observers (PDF + notification) |
| `POST` | `/api/analysis/notes/{videoId}` | Claude transcript→notes pipeline → PDF |
| `GET` | `/api/analysis/report/{reportId}` | Fetch single report (used by frontend polling) |
| `GET` | `/api/analysis/video/{videoId}/reports` | All reports for a video |

---

## Local setup

### Prerequisites
- Java 21+ (`java -version`)
- Node 18+ and npm 9+ (`node --version`)
- MongoDB 6 or 7 running on `localhost:27017`
- API keys (see below)

### 1. Clone

```powershell
git clone https://github.com/Nisha-496/contentiq.git
cd contentiq
```

### 2. Create `src/main/resources/application.properties`

The file is gitignored to keep secrets out of version control. Create it with this content (fill in your keys):

```properties
spring.application.name=contentiq
server.port=8081

# MongoDB
spring.data.mongodb.uri=mongodb://localhost:27017/contentiq_db
spring.data.mongodb.auto-index-creation=true

# Jackson
spring.jackson.serialization.write-dates-as-timestamps=false
spring.jackson.default-property-inclusion=non_null

# Logging
logging.level.com.contentiq=INFO
logging.level.org.springframework.security=INFO

# YouTube Data API v3 — get key at https://console.cloud.google.com/apis/library/youtube.googleapis.com
youtube.api.key=YOUR_YT_API_KEY
youtube.api.application-name=contentiq
youtube.comments.max-per-page=100
youtube.comments.max-total=500

# HuggingFace — get token at https://huggingface.co/settings/tokens (Read or Inference Providers scope)
huggingface.api.key=hf_YOUR_HF_TOKEN
huggingface.api.url=https://router.huggingface.co/hf-inference/models
huggingface.sentiment.model=cardiffnlp/twitter-roberta-base-sentiment-latest
huggingface.batch-size=20

# Claude (only needed for /api/analysis/notes/* — sentiment works without it)
claude.api.key=sk-ant-YOUR_KEY
claude.api.url=https://api.anthropic.com/v1/messages
claude.api.model=claude-sonnet-4-20250514
claude.api.version=2023-06-01
claude.api.max-tokens=4096
claude.retry.max-attempts=3
claude.retry.backoff-ms=1000
claude.rate-limit.max-concurrent=3
claude.rate-limit.min-interval-ms=200

# JWT — replace with a long random string (32+ chars)
security.jwt.secret=change-me-to-a-long-random-string-at-least-32-chars
security.jwt.expiration-ms=86400000

# ContentIQ
contentiq.pdf.output-dir=./generated-pdfs
contentiq.scheduler.pending-interval-ms=300000

# CORS — add your frontend origin
contentiq.cors.allowed-origins=http://localhost:5173,http://localhost:3000

# Multipart limits (large transcripts/comments)
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

### 3. Run the backend

```powershell
.\mvnw.cmd spring-boot:run
# → http://localhost:8081
```

### 4. Run the frontend (second terminal)

```powershell
cd contentiq-frontend
npm install        # first time only
npm run dev
# → http://localhost:5173
```

### 5. Use the app

Open **http://localhost:5173**, create an account, paste a YouTube URL, hit **Analyze**.

---

## API keys — where to get them

| Provider | Where | Required for | Cost |
|---|---|---|---|
| YouTube Data API v3 | [Google Cloud Console](https://console.cloud.google.com/apis/library/youtube.googleapis.com) → Enable API → Create credentials → API key | Importing videos + comments | Free (10K units/day default quota) |
| HuggingFace | [Settings → Tokens](https://huggingface.co/settings/tokens) → "Read" or fine-grained with **"Make calls to Inference Providers"** | Sentiment analysis | Free tier sufficient for demo |
| Anthropic / Claude | [console.anthropic.com](https://console.anthropic.com/) → API Keys | Notes generation from transcripts (optional) | Paid per token |

---

## Project structure

```
contentiq/
├── src/main/java/com/contentiq/contentiq/
│   ├── ContentiqApplication.java       @SpringBootApplication + @EnableScheduling
│   ├── config/                         AIClientConfig, YouTubeConfig, CorsConfig
│   ├── controller/                     Auth, Video, Comment, Analysis, YouTube
│   ├── decorator/                      AIService + Core/Logging/Retry/RateLimit
│   ├── dto/                            6 request/response DTOs
│   ├── exception/                      GlobalExceptionHandler + ResourceNotFoundException
│   ├── factory/                        ContentProcessorFactory
│   ├── model/                          Video, Comment, AnalysisReport, NotesDocument, User, Notification
│   ├── observer/                       AnalysisObserver + PDF/Notification + EventPublisher
│   ├── repository/                     6 MongoRepositories
│   ├── scheduler/                      PendingAnalysisScheduler
│   ├── security/                       JwtUtil, JwtFilter, JwtUserDetailsService, SecurityConfig
│   ├── service/                        Video, Comment, User, AIAnalysis, NotesGenerator (abstract),
│   │                                   AINotesGenerator, Notification, YouTube, HuggingFaceSentimentClient
│   ├── strategy/                       AnalysisStrategy + Sentiment/CommentSummary/NotesGeneration
│   └── util/                           VideoUrlParser
├── src/main/resources/
│   └── application.properties          (gitignored — create from template above)
├── contentiq-frontend/
│   ├── src/
│   │   ├── api/client.js               axios + JWT interceptor
│   │   ├── context/AuthContext.jsx     localStorage-backed auth state
│   │   ├── components/                 Layout, ProtectedRoute, SentimentCard, SummaryBar
│   │   └── pages/                      Login, Dashboard, History
│   ├── tailwind.config.js              Dark theme + brand palette
│   └── vite.config.js                  Dev server on :5173
├── pom.xml
└── README.md
```

---

## Verified end-to-end results

Live test against `https://www.youtube.com/watch?v=kJQP7kiw5Fk` (Luis Fonsi — "Despacito") with 30 comments:

```
status     = COMPLETED
total      = 30
POSITIVE   = 8   (27%)
NEGATIVE   = 1   (3%)
QUESTIONS  = 6   (20%)     ← client-side filter on '?' / WH-words
NEUTRAL    = 15  (50%)
```

PDF generated at `generated-pdfs/sentiment_*.pdf`, deterministic summary persisted to MongoDB, notification fired via observer chain.

---

## Notes & limitations

- **HuggingFace cold starts**: first request to a long-idle model can take 10–30s. Subsequent calls return in ~500ms.
- **Question detection is client-side**: backend stores `category=GENERAL` for all comments. The frontend filters on text shape. Swap in a multi-label classifier if you want backend-side category persistence.
- **Unit tests**: 50 tests cover `VideoUrlParser`, `JwtUtil`, the `HuggingFaceSentimentClient` response parser, `CommentSummaryStrategy`, and `SentimentAnalysisStrategy` (mocked HF). Run with `./mvnw test`. CI runs them on every push via [GitHub Actions](.github/workflows/ci.yml). Integration-level coverage (controllers, Mongo interactions) is **not yet** included — those were verified manually with curl + the browser.
- **iText 8 license**: AGPL for OSS use, commercial license required for closed-source production deployment.

---

## License

Personal / educational project. Verify third-party license terms (iText AGPL, model licenses on HuggingFace) before any commercial use.
