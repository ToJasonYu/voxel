import { graphql } from "../generated";

// Written as plain strings passed through the generated `graphql()` tag --
// codegen scans for exactly these calls and produces matching typed documents
// (and, for the fragment, a matching TS type) in src/generated.

// Shared shape used by every operation that returns a widget, so the three
// places widgets show up (session load, live push, mutation result) can't
// silently drift out of sync with each other.
export const WIDGET_FIELDS = graphql(`
  fragment WidgetFields on Widget {
    id
    title
    chartType
    data {
      label
      value
    }
  }
`);

export const CREATE_SESSION = graphql(`
  mutation CreateSession {
    createSession {
      id
      widgets {
        id
      }
    }
  }
`);

export const GET_SESSION = graphql(`
  query GetSession($id: ID!) {
    session(id: $id) {
      id
      widgets {
        ...WidgetFields
      }
    }
  }
`);

export const SUBMIT_VOICE_COMMAND = graphql(`
  mutation SubmitVoiceCommand($sessionId: ID!, $transcript: String!) {
    submitVoiceCommand(sessionId: $sessionId, transcript: $transcript) {
      success
      errorMessage
      widget {
        ...WidgetFields
      }
    }
  }
`);

export const DASHBOARD_UPDATED = graphql(`
  subscription DashboardUpdated($sessionId: ID!) {
    dashboardUpdated(sessionId: $sessionId) {
      ...WidgetFields
    }
  }
`);
