# Voxel

Voxel is a collaborative data dashboard you talk to. Type or speak a command like *"show
signups by month as a bar chart,"* and a chart appears: live, immediately, for everyone
else looking at the same dashboard link.

## Architecture

- **Backend:** Kotlin, using `graphql-kotlin` for the GraphQL layer. Schema types are plain
  Kotlin data classes — the GraphQL SDL is generated from their shape via reflection, so
  there's no separate schema file to hand-maintain.
- **Live sync:** every client watching a dashboard holds open a GraphQL **subscription**
  over a WebSocket (the `graphql-transport-ws` protocol). When any client's command produces
  a new chart, it's published to an in-process Kotlin `Flow` (`MutableSharedFlow`), and every
  subscription for that session — filtered down from that one shared stream — pushes the new
  widget to its client immediately. No polling, no message broker: a single backend instance
  doesn't need one, and that in-process `Flow` is exactly the piece that would move to
  something like Redis pub/sub if this ever needed multiple backend instances.
- **Database:** PostgreSQL via Exposed.
- **Frontend:** React + TypeScript (Vite), Apollo Client. A single Apollo Client routes
  queries and mutations over plain HTTP and subscriptions over WebSocket, using Apollo's
  `split` link to send each operation over the right transport. Types are generated directly
  from the backend's GraphQL schema via GraphQL Code Generator, so a schema change surfaces
  as a frontend compile error, not a silent runtime mismatch.
- **Charts:** Recharts.
- **Input:** typed text, or the browser's built-in `SpeechRecognition` API for voice —
  speech-to-text happens entirely client-side; only the resulting text ever reaches the
  backend.
- **Local dev:** Docker Compose (Postgres + backend + frontend) — intentionally not
  Kubernetes, since one backend service and one database don't need that operational
  overhead.

## How a command becomes a chart

1. A transcript (typed or spoken) is sent to the backend via a `submitVoiceCommand` GraphQL
   mutation.
2. The backend asks an LLM to turn that transcript into a candidate SQL query against a
   small, fixed schema.
3. That SQL is validated and rewritten before it's ever allowed to run — since LLM output is
   external input, it's checked the same way any other untrusted input would be, rather than
   trusted outright.
4. The validated query is persisted (not its result — the query itself), and the resulting
   widget is saved and published to `WidgetEvents`.
5. Every client subscribed to `dashboardUpdated(sessionId)` for that dashboard receives the
   new widget over its open WebSocket connection, immediately.

Widgets store their query, not a cached result — every time a widget loads, its query is
re-run fresh, so charts stay live if the underlying data changes, and there's exactly one
source of truth for what a chart shows.

## Tests

`backend/src/test/kotlin/com/voxel/sql/SqlValidatorTest.kt` and
`llm/LlmSqlGeneratorTest.kt` cover the SQL validation and generation logic, including a
couple of edge cases (an aggregate `JOIN` query, an `ORDER BY` referencing a `SELECT`-item
alias) that came up during development.

## Running it locally

[... same as before ...]

## Non-goals

- No support for arbitrary schemas — the allowlist is fixed to the three seeded tables.
- No fine-tuned model — one LLM API call is the entire "AI" surface area.
- No authentication — a session id (shareable link) is the whole access model, appropriate
  for this project's scope.
