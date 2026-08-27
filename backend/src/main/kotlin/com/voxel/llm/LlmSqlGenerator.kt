package com.voxel.llm

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// Documents the schema for the model -- kept as plain text here rather than derived
// from SchemaAllowlist, since the prompt also wants readable per-table grouping,
// which that flat allowlist deliberately doesn't have.
private const val SCHEMA_DESCRIPTION = """
customers(id, name, signup_date, plan_tier)
orders(id, customer_id, order_date, total_amount, status)
order_items(id, order_id, product_name, quantity, unit_price)
"""

private val SYSTEM_PROMPT = """
    You translate a spoken dashboard command into a single read-only PostgreSQL
    SELECT query against exactly this schema:
    $SCHEMA_DESCRIPTION
    Rules:
    - Output ONLY the SQL query. No markdown, no code fences, no explanation.
    - Only reference the tables and columns listed above.
    - Never write INSERT, UPDATE, DELETE, or any DDL -- SELECT only.
    - For chart-style requests, prefer an aggregate (COUNT, SUM, AVG) with GROUP BY.
""".trimIndent()

/**
 * Thin wrapper around the Anthropic Messages API: transcript in, candidate SQL
 * string out. Deliberately dumb -- it has no idea what "safe" SQL looks like.
 * Everything interesting happens afterward, in SqlValidator.
 */
class LlmSqlGenerator(
    private val apiKey: String = System.getenv("ANTHROPIC_API_KEY").orEmpty(),
    private val model: String = "claude-sonnet-4-5",
    private val httpClient: HttpClient = HttpClient(CIO) {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }
) {
    suspend fun generateSql(transcript: String): String {
        check(apiKey.isNotBlank()) { "ANTHROPIC_API_KEY is not set." }

        val response = httpClient.post("https://api.anthropic.com/v1/messages") {
            header("x-api-key", apiKey)
            header("anthropic-version", "2023-06-01")
            contentType(ContentType.Application.Json)
            setBody(
                AnthropicRequest(
                    model = model,
                    maxTokens = 300,
                    system = SYSTEM_PROMPT,
                    messages = listOf(AnthropicMessage(role = "user", content = transcript))
                )
            )
        }.body<AnthropicResponse>()

        val text = response.content.firstOrNull { it.type == "text" }?.text
            ?: error("Anthropic response had no text content: $response")
        return stripCodeFences(text)
    }

    // Models are told not to, but often wrap SQL in ```sql fences anyway -- strip
    // them here rather than teaching SqlValidator anything about markdown.
    private fun stripCodeFences(text: String): String =
        text.trim().removeSurrounding("```sql", "```").removeSurrounding("```", "```").trim()
}

@Serializable
private data class AnthropicRequest(
    val model: String,
    @SerialName("max_tokens") val maxTokens: Int,
    val system: String,
    val messages: List<AnthropicMessage>
)

@Serializable
private data class AnthropicMessage(val role: String, val content: String)

@Serializable
private data class AnthropicResponse(val content: List<AnthropicContentBlock>)

@Serializable
private data class AnthropicContentBlock(val type: String, val text: String? = null)
