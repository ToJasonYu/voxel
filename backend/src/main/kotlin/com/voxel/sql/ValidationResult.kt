package com.voxel.sql

/**
 * Outcome of validating a candidate SQL string. A sealed class instead of a
 * boolean+nullable-string pair so the compiler forces every caller to handle
 * both cases explicitly (a `when` over this is checked exhaustively).
 */
sealed class ValidationResult {
    /** [sql] is the LLM's query, re-serialized and wrapped with a row limit -- safe to execute. */
    data class Accepted(val sql: String) : ValidationResult()

    data class Rejected(val reason: String) : ValidationResult()
}
