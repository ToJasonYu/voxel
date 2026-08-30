package com.voxel.graphql

import com.expediagroup.graphql.generator.scalars.ID
import com.expediagroup.graphql.server.operations.Mutation
import com.voxel.db.SessionRepository
import com.voxel.db.WidgetRepository
import com.voxel.llm.LlmSqlGenerator
import com.voxel.sql.SqlValidator
import com.voxel.sql.ValidationResult
import java.util.UUID

class DashboardMutation(
    private val llmSqlGenerator: LlmSqlGenerator = LlmSqlGenerator()
) : Mutation {

    suspend fun createSession(): DashboardSession {
        val sessionId = SessionRepository.create()
        return DashboardSession(ID(sessionId.toString()), emptyList())
    }

    // This is the whole voice-command pipeline: transcript -> candidate SQL (LlmSqlGenerator)
    // -> accept/reject (SqlValidator) -> persist + notify subscribers. Neither of the first two
    // steps knows the other exists; this function is the only place that wires them together.
    suspend fun submitVoiceCommand(sessionId: ID, transcript: String): VoiceCommandResult {
        val parsedSessionId = UUID.fromString(sessionId.value)

        // The Anthropic call is a real network boundary (timeouts, rate limits, bad
        // auth) -- worth catching here so a flaky request becomes a normal failed
        // result instead of a 500 from the GraphQL endpoint.
        val candidateSql = try {
            llmSqlGenerator.generateSql(transcript)
        } catch (e: Exception) {
            return VoiceCommandResult(false, null, "Could not generate SQL: ${e.message}")
        }

        val validatedSql = when (val validation = SqlValidator.validate(candidateSql)) {
            is ValidationResult.Rejected -> return VoiceCommandResult(false, null, validation.reason)
            is ValidationResult.Accepted -> validation.sql
        }

        val row = WidgetRepository.insert(
            sessionId = parsedSessionId,
            chartType = inferChartType(transcript).name,
            title = inferTitle(transcript),
            sqlQuery = validatedSql
        )
        val widget = row.toGraphQLWidget()

        WidgetEvents.publish(parsedSessionId, widget) // wakes up every dashboardUpdated subscriber on this session

        return VoiceCommandResult(true, widget, null)
    }
}
