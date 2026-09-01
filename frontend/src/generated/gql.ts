/* eslint-disable */
import * as types from './graphql';
import type { TypedDocumentNode as DocumentNode } from '@graphql-typed-document-node/core';

/**
 * Map of all GraphQL operations in the project.
 *
 * This map has several performance disadvantages:
 * 1. It is not tree-shakeable, so it will include all operations in the project.
 * 2. It is not minifiable, so the string of a GraphQL query will be multiple times inside the bundle.
 * 3. It does not support dead code elimination, so it will add unused operations.
 *
 * Therefore it is highly recommended to use the babel or swc plugin for production.
 * Learn more about it here: https://the-guild.dev/graphql/codegen/plugins/presets/preset-client#reducing-bundle-size
 */
type Documents = {
    "\n  fragment WidgetFields on Widget {\n    id\n    title\n    chartType\n    data {\n      label\n      value\n    }\n  }\n": typeof types.WidgetFieldsFragmentDoc,
    "\n  mutation CreateSession {\n    createSession {\n      id\n      widgets {\n        id\n      }\n    }\n  }\n": typeof types.CreateSessionDocument,
    "\n  query GetSession($id: ID!) {\n    session(id: $id) {\n      id\n      widgets {\n        ...WidgetFields\n      }\n    }\n  }\n": typeof types.GetSessionDocument,
    "\n  mutation SubmitVoiceCommand($sessionId: ID!, $transcript: String!) {\n    submitVoiceCommand(sessionId: $sessionId, transcript: $transcript) {\n      success\n      errorMessage\n      widget {\n        ...WidgetFields\n      }\n    }\n  }\n": typeof types.SubmitVoiceCommandDocument,
    "\n  subscription DashboardUpdated($sessionId: ID!) {\n    dashboardUpdated(sessionId: $sessionId) {\n      ...WidgetFields\n    }\n  }\n": typeof types.DashboardUpdatedDocument,
};
const documents: Documents = {
    "\n  fragment WidgetFields on Widget {\n    id\n    title\n    chartType\n    data {\n      label\n      value\n    }\n  }\n": types.WidgetFieldsFragmentDoc,
    "\n  mutation CreateSession {\n    createSession {\n      id\n      widgets {\n        id\n      }\n    }\n  }\n": types.CreateSessionDocument,
    "\n  query GetSession($id: ID!) {\n    session(id: $id) {\n      id\n      widgets {\n        ...WidgetFields\n      }\n    }\n  }\n": types.GetSessionDocument,
    "\n  mutation SubmitVoiceCommand($sessionId: ID!, $transcript: String!) {\n    submitVoiceCommand(sessionId: $sessionId, transcript: $transcript) {\n      success\n      errorMessage\n      widget {\n        ...WidgetFields\n      }\n    }\n  }\n": types.SubmitVoiceCommandDocument,
    "\n  subscription DashboardUpdated($sessionId: ID!) {\n    dashboardUpdated(sessionId: $sessionId) {\n      ...WidgetFields\n    }\n  }\n": types.DashboardUpdatedDocument,
};

/**
 * The graphql function is used to parse GraphQL queries into a document that can be used by GraphQL clients.
 *
 *
 * @example
 * ```ts
 * const query = graphql(`query GetUser($id: ID!) { user(id: $id) { name } }`);
 * ```
 *
 * The query argument is unknown!
 * Please regenerate the types.
 */
export function graphql(source: string): unknown;

/**
 * The graphql function is used to parse GraphQL queries into a document that can be used by GraphQL clients.
 */
export function graphql(source: "\n  fragment WidgetFields on Widget {\n    id\n    title\n    chartType\n    data {\n      label\n      value\n    }\n  }\n"): (typeof documents)["\n  fragment WidgetFields on Widget {\n    id\n    title\n    chartType\n    data {\n      label\n      value\n    }\n  }\n"];
/**
 * The graphql function is used to parse GraphQL queries into a document that can be used by GraphQL clients.
 */
export function graphql(source: "\n  mutation CreateSession {\n    createSession {\n      id\n      widgets {\n        id\n      }\n    }\n  }\n"): (typeof documents)["\n  mutation CreateSession {\n    createSession {\n      id\n      widgets {\n        id\n      }\n    }\n  }\n"];
/**
 * The graphql function is used to parse GraphQL queries into a document that can be used by GraphQL clients.
 */
export function graphql(source: "\n  query GetSession($id: ID!) {\n    session(id: $id) {\n      id\n      widgets {\n        ...WidgetFields\n      }\n    }\n  }\n"): (typeof documents)["\n  query GetSession($id: ID!) {\n    session(id: $id) {\n      id\n      widgets {\n        ...WidgetFields\n      }\n    }\n  }\n"];
/**
 * The graphql function is used to parse GraphQL queries into a document that can be used by GraphQL clients.
 */
export function graphql(source: "\n  mutation SubmitVoiceCommand($sessionId: ID!, $transcript: String!) {\n    submitVoiceCommand(sessionId: $sessionId, transcript: $transcript) {\n      success\n      errorMessage\n      widget {\n        ...WidgetFields\n      }\n    }\n  }\n"): (typeof documents)["\n  mutation SubmitVoiceCommand($sessionId: ID!, $transcript: String!) {\n    submitVoiceCommand(sessionId: $sessionId, transcript: $transcript) {\n      success\n      errorMessage\n      widget {\n        ...WidgetFields\n      }\n    }\n  }\n"];
/**
 * The graphql function is used to parse GraphQL queries into a document that can be used by GraphQL clients.
 */
export function graphql(source: "\n  subscription DashboardUpdated($sessionId: ID!) {\n    dashboardUpdated(sessionId: $sessionId) {\n      ...WidgetFields\n    }\n  }\n"): (typeof documents)["\n  subscription DashboardUpdated($sessionId: ID!) {\n    dashboardUpdated(sessionId: $sessionId) {\n      ...WidgetFields\n    }\n  }\n"];

export function graphql(source: string) {
  return (documents as any)[source] ?? {};
}

export type DocumentType<TDocumentNode extends DocumentNode<any, any>> = TDocumentNode extends DocumentNode<  infer TType,  any>  ? TType  : never;