import { useMemo, useState, type FormEvent } from "react";
import { useMutation, useQuery, useSubscription } from "@apollo/client/react";
import { DASHBOARD_UPDATED, GET_SESSION, SUBMIT_VOICE_COMMAND } from "../graphql/operations";
import type { WidgetFieldsFragment } from "../generated/graphql";
import { MicButton } from "./MicButton";
import { WidgetCard } from "./WidgetCard";

function addWidget(current: WidgetFieldsFragment[], widget: WidgetFieldsFragment): WidgetFieldsFragment[] {
  // A widget this client just created can arrive twice: once in the mutation's own
  // response, once again over the subscription every client (including this one)
  // is listening on. De-dupe by id so it doesn't render twice.
  return current.some((w) => w.id === widget.id) ? current : [...current, widget];
}

export function Dashboard({ sessionId }: { sessionId: string }) {
  const { data, loading, error } = useQuery(GET_SESSION, { variables: { id: sessionId } });
  // Only widgets that arrived *after* the initial load -- via a live push or this
  // client's own mutation -- live in state. The full list is derived at render time
  // by folding these onto the query's own widgets, so there's no effect syncing
  // query data into state (an antipattern: it causes an extra render every time).
  const [liveWidgets, setLiveWidgets] = useState<WidgetFieldsFragment[]>([]);
  const [submitVoiceCommand, { loading: submitting }] = useMutation(SUBMIT_VOICE_COMMAND);
  const [lastError, setLastError] = useState<string | null>(null);
  const [textCommand, setTextCommand] = useState("");

  const widgets = useMemo(
    () => liveWidgets.reduce(addWidget, data?.session?.widgets ?? []),
    [data, liveWidgets]
  );

  useSubscription(DASHBOARD_UPDATED, {
    variables: { sessionId },
    onData: ({ data: subscriptionData }) => {
      const widget = subscriptionData.data?.dashboardUpdated;
      if (widget) setLiveWidgets((current) => addWidget(current, widget));
    },
  });

  async function handleCommand(transcript: string) {
    setLastError(null);
    const { data: result } = await submitVoiceCommand({ variables: { sessionId, transcript } });
    const outcome = result?.submitVoiceCommand;
    if (outcome?.success && outcome.widget) {
      setLiveWidgets((current) => addWidget(current, outcome.widget!));
    } else {
      setLastError(outcome?.errorMessage ?? "Something went wrong.");
    }
  }

  function handleTextSubmit(event: FormEvent) {
    event.preventDefault();
    const transcript = textCommand.trim();
    if (!transcript) return;
    setTextCommand("");
    void handleCommand(transcript);
  }

  if (loading) return <p>Loading dashboard...</p>;
  if (error) return <p>Failed to load dashboard: {error.message}</p>;

  return (
    <div className="dashboard">
      <header>
        <h1>Voxel</h1>
        <p className="session-link">
          Share this link to collaborate live: <code>{window.location.href}</code>
        </p>
      </header>

      <div className="command-bar">
        <MicButton onTranscript={handleCommand} disabled={submitting} />
        <form onSubmit={handleTextSubmit}>
          <input
            className="text-input"
            value={textCommand}
            onChange={(event) => setTextCommand(event.target.value)}
            placeholder="...or type a command, e.g. 'show registrations by month as a bar chart'"
            disabled={submitting}
          />
          <button className="btn-primary" type="submit" disabled={submitting}>
            Send
          </button>
        </form>
        {lastError && <p className="error">{lastError}</p>}
      </div>

      <div className="widget-grid">
        {widgets.map((widget) => (
          <WidgetCard key={widget.id} widget={widget} />
        ))}
        {widgets.length === 0 && <p>No widgets yet -- try a voice command above.</p>}
      </div>
    </div>
  );
}
