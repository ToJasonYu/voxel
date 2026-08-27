package com.voxel.sql

import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SqlValidatorTest {

    @Test
    fun `accepts a plain select and wraps it with a row limit`() {
        val result = SqlValidator.validate("SELECT id, name FROM customers")
        val accepted = assertIs<ValidationResult.Accepted>(result)
        assertTrue(accepted.sql.contains("LIMIT ${SchemaAllowlist.MAX_ROW_LIMIT}"))
    }

    @Test
    fun `rejects a write statement`() {
        val result = SqlValidator.validate("DELETE FROM customers WHERE id = 1")
        assertIs<ValidationResult.Rejected>(result)
    }

    @Test
    fun `rejects a multi-statement injection attempt`() {
        val result = SqlValidator.validate("SELECT * FROM customers; DROP TABLE customers;")
        assertIs<ValidationResult.Rejected>(result)
    }

    @Test
    fun `rejects a query referencing an unknown table`() {
        val result = SqlValidator.validate("SELECT * FROM dashboard_widgets")
        assertIs<ValidationResult.Rejected>(result)
    }

    @Test
    fun `rejects a query referencing an unknown column`() {
        val result = SqlValidator.validate("SELECT credit_card_number FROM customers")
        assertIs<ValidationResult.Rejected>(result)
    }

    @Test
    fun `accepts a join with a group by and an aggregate function`() {
        val result = SqlValidator.validate(
            """
            SELECT c.plan_tier, COUNT(*) AS order_count
            FROM customers c
            JOIN orders o ON o.customer_id = c.id
            WHERE o.status = 'completed'
            GROUP BY c.plan_tier
            ORDER BY order_count DESC
            """.trimIndent()
        )
        assertIs<ValidationResult.Accepted>(result)
    }
}
