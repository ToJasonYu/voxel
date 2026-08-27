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
}
