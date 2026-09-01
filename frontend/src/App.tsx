import { useState, type FormEvent } from "react";
import { useMutation } from "@apollo/client/react";
import { CREATE_SESSION } from "./graphql/operations";
import { Dashboard } from "./components/Dashboard";
import "./App.css";

function readSessionIdFromUrl(): string | null {
  return new URLSearchParams(window.location.search).get("session");
}

function putSessionIdInUrl(id: string) {
  const url = new URL(window.location.href);
  url.searchParams.set("session", id);
  window.history.pushState({}, "", url);
}

export default function App() {
  const [sessionId, setSessionId] = useState<string | null>(readSessionIdFromUrl);
  const [createSession, { loading: creating }] = useMutation(CREATE_SESSION);
  const [joinId, setJoinId] = useState("");

  async function handleCreate() {
    const { data } = await createSession();
    const id = data?.createSession.id;
    if (id) {
      setSessionId(id);
      putSessionIdInUrl(id);
    }
  }

  function handleJoin(event: FormEvent) {
    event.preventDefault();
    const id = joinId.trim();
    if (!id) return;
    setSessionId(id);
    putSessionIdInUrl(id);
  }

  if (sessionId) {
    return <Dashboard sessionId={sessionId} />;
  }

  return (
    <div className="session-gate">
      <h1>Voxel</h1>
      <p>Speak a command, watch a chart appear -- shared live with anyone on the same session link.</p>
      <button onClick={handleCreate} disabled={creating}>
        {creating ? "Creating..." : "Create new dashboard"}
      </button>
      <form onSubmit={handleJoin}>
        <input value={joinId} onChange={(event) => setJoinId(event.target.value)} placeholder="...or paste a session id to join" />
        <button type="submit">Join</button>
      </form>
    </div>
  );
}
