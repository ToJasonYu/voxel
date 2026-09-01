import type { CodegenConfig } from "@graphql-codegen/cli";

// Schema comes from a checked-in SDL snapshot (frontend/schema.graphql), not a live
// query against the backend -- so `npm run codegen` (and the frontend build) never
// needs the backend running. Re-fetch that file (GET /sdl) if the backend schema changes.
const config: CodegenConfig = {
  schema: "./schema.graphql",
  documents: ["src/**/*.tsx", "src/**/*.ts"],
  generates: {
    "./src/generated/": {
      preset: "client",
      // Fragment masking (Apollo's default) hides a fragment's fields behind an
      // unmasking call -- more than this project needs. Off, so a component using
      // WidgetFields just sees its fields directly on the result type.
      presetConfig: { fragmentMasking: false },
      // tsconfig has verbatimModuleSyntax on, which requires type-only imports
      // to say so explicitly -- this makes codegen emit `import type` for them.
      config: { useTypeImports: true },
    },
  },
  ignoreNoDocuments: true,
};

export default config;
