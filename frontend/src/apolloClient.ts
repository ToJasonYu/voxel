import { ApolloClient, HttpLink, InMemoryCache, split } from "@apollo/client";
import { GraphQLWsLink } from "@apollo/client/link/subscriptions";
import { getMainDefinition } from "@apollo/client/utilities";
import { createClient } from "graphql-ws";

const httpUrl = import.meta.env.VITE_GRAPHQL_HTTP_URL ?? "http://localhost:8080/graphql";
const wsUrl = import.meta.env.VITE_GRAPHQL_WS_URL ?? "ws://localhost:8080/subscriptions";

const httpLink = new HttpLink({ uri: httpUrl });
const wsLink = new GraphQLWsLink(createClient({ url: wsUrl }));

// A single ApolloClient has one link, so `split` routes each request by shape:
// subscriptions go over the websocket (graphql-ws, matching the backend's
// graphql-transport-ws protocol), queries and mutations go over plain HTTP.
const splitLink = split(
  ({ query }) => {
    const definition = getMainDefinition(query);
    return definition.kind === "OperationDefinition" && definition.operation === "subscription";
  },
  wsLink,
  httpLink
);

export const apolloClient = new ApolloClient({
  link: splitLink,
  cache: new InMemoryCache(),
});
