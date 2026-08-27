package com.voxel.sql

import net.sf.jsqlparser.JSQLParserException
import net.sf.jsqlparser.parser.CCJSqlParserUtil
import net.sf.jsqlparser.statement.select.Select

/**
 * Gatekeeper between the LLM's SQL output and the database. Nothing the LLM
 * returns runs as-is -- it has to pass through here first, and this is the
 * only place that decides "yes" or "no".
 */
object SqlValidator {

    fun validate(rawSql: String): ValidationResult {
        // CCJSqlParserUtil.parse() expects exactly one statement and nothing
        // trailing after it, so "SELECT ...; DROP TABLE ..." fails to parse
        // right here rather than needing a separate multi-statement check.
        val statement = try {
            CCJSqlParserUtil.parse(rawSql)
        } catch (e: JSQLParserException) {
            return ValidationResult.Rejected("Does not parse as a single valid SQL statement.")
        }

        // Anything other than SELECT -- INSERT, UPDATE, DELETE, DROP, etc. -- is rejected here.
        // We don't try to detect "is this secretly a write" by inspecting keywords; the
        // statement's Kotlin/Java type already tells us that reliably.
        if (statement !is Select) {
            return ValidationResult.Rejected("Only SELECT statements are allowed.")
        }

        val safeSql = wrapWithRowLimit(statement.toString())
        return ValidationResult.Accepted(safeSql)
    }

    // Wrapping in an outer subquery works for any validated SELECT shape (plain,
    // WITH clause, UNION, ...) without needing to know how to edit that shape's
    // own LIMIT clause. An inner LIMIT smaller than ours still wins, since LIMIT
    // only ever shrinks a result set, never grows it.
    private fun wrapWithRowLimit(innerSql: String): String =
        "SELECT * FROM ($innerSql) AS voxel_query LIMIT ${SchemaAllowlist.MAX_ROW_LIMIT}"
}
