package com.voxel.llm

import com.voxel.sql.SqlValidator
import com.voxel.sql.ValidationResult
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * No live Anthropic calls here (no API key in this environment) -- instead we stub
 * the HTTP layer with canned responses shaped like what the model would plausibly
 * return for a few example transcripts, and check both ends: that the wrapper
 * extracts clean SQL from the response, and that SqlValidator correctly accepts or
 * rejects what comes out. Live verification against the real API is a follow-up
 * once an ANTHROPIC_API_KEY is available.
 */
class LlmSqlGeneratorTest {

    private fun generatorReturning(responseText: String): LlmSqlGenerator {
        val mockEngine = MockEngine { _ ->
            respond(
                content = anthropicResponseJson(responseText),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }
        return LlmSqlGenerator(apiKey = "test-key", httpClient = client)
    }

    @Test
    fun `strips markdown code fences from the model's response`() = runTest {
        val generator = generatorReturning("```sql\nSELECT plan_tier, COUNT(*) FROM customers GROUP BY plan_tier\n```")
        val sql = generator.generateSql("show me customers by plan tier")
        assertEquals("SELECT plan_tier, COUNT(*) FROM customers GROUP BY plan_tier", sql)
    }

    @Test
    fun `show signups by month as a bar chart -- plausible response passes validation`() = runTest {
        val generator = generatorReturning(
            "SELECT date_trunc('month', signup_date) AS month, COUNT(*) AS signups " +
                "FROM customers GROUP BY month ORDER BY month"
        )
        val sql = generator.generateSql("show signups by month as a bar chart")
        assertIs<ValidationResult.Accepted>(SqlValidator.validate(sql))
    }

    @Test
    fun `filter to this year -- plausible response passes validation`() = runTest {
        val generator = generatorReturning(
            "SELECT * FROM orders WHERE order_date >= date_trunc('year', now())"
        )
        val sql = generator.generateSql("filter to this year")
        assertIs<ValidationResult.Accepted>(SqlValidator.validate(sql))
    }

    @Test
    fun `a hallucinated or unsafe response is still caught by the validator`() = runTest {
        // Simulates the model ignoring the prompt's rules -- SqlValidator is the real
        // backstop here, not the prompt.
        val generator = generatorReturning("DELETE FROM customers WHERE plan_tier = 'free'")
        val sql = generator.generateSql("get rid of free tier customers")
        assertIs<ValidationResult.Rejected>(SqlValidator.validate(sql))
    }

    private fun anthropicResponseJson(text: String): String {
        val escaped = text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
        return """{"content":[{"type":"text","text":"$escaped"}]}"""
    }
}
