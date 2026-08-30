package com.voxel.graphql

import com.expediagroup.graphql.generator.scalars.ID
import com.voxel.db.WidgetRow
import com.voxel.sql.SandboxExecutor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Turns a persisted widget row into the GraphQL Widget clients see, by re-running
 * its stored query right now rather than reading a cached result -- see the
 * comment on WidgetRepository.insert for why. Assumes the query returns exactly
 * two columns (label, value), which is what LlmSqlGenerator's prompt asks for.
 */
suspend fun WidgetRow.toGraphQLWidget(): Widget = withContext(Dispatchers.IO) {
    val rows = SandboxExecutor.execute(sqlQuery)
    val data = rows.map { row ->
        val values = row.values.toList()
        DataPoint(
            label = values.getOrNull(0).toLabelString(),
            value = values.getOrNull(1).toNumericValue()
        )
    }
    Widget(
        id = ID(id.toString()),
        chartType = ChartType.valueOf(chartType.uppercase()),
        title = title,
        data = data
    )
}

private fun Any?.toNumericValue(): Double = when (this) {
    null -> 0.0
    is Number -> toDouble()
    else -> toString().toDoubleOrNull() ?: 0.0
}

// A date_trunc('month', ...) result comes back from JDBC as java.sql.Timestamp,
// whose toString() is e.g. "2025-03-01 00:00:00.0" -- fine for SQL, noisy as a
// chart label, so dates get trimmed down to just the date part here.
private fun Any?.toLabelString(): String = when (this) {
    null -> ""
    is java.sql.Timestamp -> toLocalDateTime().toLocalDate().toString()
    is java.sql.Date -> toLocalDate().toString()
    else -> toString()
}
