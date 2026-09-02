# Codegen output (src/generated) is checked into the repo, so this build never
# needs a live backend -- just `npm run build`, same as building locally.
FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .

# Baked in at build time (Vite convention) -- the values here are host-reachable
# addresses, since the frontend runs in the *browser*, not inside the Docker
# network, so "backend:8080" (the internal service name) wouldn't resolve for it.
ARG VITE_GRAPHQL_HTTP_URL=http://localhost:8080/graphql
ARG VITE_GRAPHQL_WS_URL=ws://localhost:8080/subscriptions
ENV VITE_GRAPHQL_HTTP_URL=$VITE_GRAPHQL_HTTP_URL
ENV VITE_GRAPHQL_WS_URL=$VITE_GRAPHQL_WS_URL
RUN npm run build

FROM node:22-alpine
WORKDIR /app
RUN npm install -g serve
COPY --from=build /app/dist ./dist

EXPOSE 5173
CMD ["serve", "-s", "dist", "-l", "5173"]
