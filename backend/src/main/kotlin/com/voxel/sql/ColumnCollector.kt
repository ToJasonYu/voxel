package com.voxel.sql

import net.sf.jsqlparser.expression.ExpressionVisitorAdapter
import net.sf.jsqlparser.schema.Column
import net.sf.jsqlparser.statement.select.ParenthesedSelect
import net.sf.jsqlparser.statement.select.PlainSelect
import net.sf.jsqlparser.statement.select.Select

/**
 * Walks a SELECT's clauses and collects every column it references, so
 * SqlValidator can check each one against the allowlist. We only override
 * visit(Column) -- ExpressionVisitorAdapter's default behavior already
 * recurses into function args, CASE branches, arithmetic, etc., so a column
 * buried in e.g. date_trunc('month', signup_date) still gets found.
 */
private class ColumnCollector : ExpressionVisitorAdapter() {
    val found = mutableListOf<Column>()

    override fun visit(column: Column) {
        found += column
    }
}

fun collectColumns(select: Select): List<Column> {
    if (select !is PlainSelect) return emptyList() // UNIONs etc: table-level allowlist still applies

    val collector = ColumnCollector()
    select.selectItems.forEach { it.expression?.accept(collector) }
    select.where?.accept(collector)
    select.having?.accept(collector)
    select.groupBy?.groupByExpressionList?.forEach { it.accept(collector) } // ExpressionList is itself a List<Expression>
    select.orderByElements?.forEach { it.expression?.accept(collector) }
    select.joins?.forEach { join -> join.onExpressions?.forEach { it.accept(collector) } }

    val nested = mutableListOf<Column>()
    (select.fromItem as? ParenthesedSelect)?.select?.let { nested += collectColumns(it) }
    select.joins?.mapNotNull { it.rightItem as? ParenthesedSelect }?.forEach { nested += collectColumns(it.select) }

    return collector.found + nested
}
