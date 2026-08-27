package com.voxel.sql

import net.sf.jsqlparser.JSQLParserException
import net.sf.jsqlparser.parser.CCJSqlParserUtil
import net.sf.jsqlparser.statement.Statement
import net.sf.jsqlparser.util.TablesNamesFinder
import net.sf.jsqlparser.statement.select.PlainSelect
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

        // Cast to Statement explicitly: Select also implements Expression, and
        // TablesNamesFinder overloads on both, which the compiler can't disambiguate on its own.
        val referencedTables = TablesNamesFinder().getTables(statement as Statement)
        val unknownTable = referencedTables.firstOrNull { it.lowercase() !in SchemaAllowlist.TABLES }
        if (unknownTable != null) {
            return ValidationResult.Rejected("Unknown table: $unknownTable")
        }

        // ORDER BY / HAVING can reference a SELECT item's own alias (e.g. "ORDER BY
        // order_count") rather than a real column, so those aliases count as allowed too.
        val outputAliases = (statement as? PlainSelect)?.selectItems
            ?.mapNotNull { it.alias?.name?.lowercase() }
            ?.toSet() ?: emptySet()

        val unknownColumn = collectColumns(statement)
            .map { it.columnName.lowercase() }
            .firstOrNull { it !in SchemaAllowlist.COLUMNS && it !in outputAliases }
        if (unknownColumn != null) {
            return ValidationResult.Rejected("Unknown column: $unknownColumn")
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
