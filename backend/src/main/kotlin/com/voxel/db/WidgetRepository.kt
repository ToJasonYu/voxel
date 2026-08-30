package com.voxel.db

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.Instant
import java.util.UUID

/** A persisted widget row. Deliberately has no `data` field -- see WidgetRepository.insert's comment. */
data class WidgetRow(
    val id: UUID,
    val chartType: String,
    val title: String,
    val sqlQuery: String,
    val position: Int
)

object WidgetRepository {
    suspend fun insert(sessionId: UUID, chartType: String, title: String, sqlQuery: String): WidgetRow =
        newSuspendedTransaction(Dispatchers.IO) {
            val nextPosition = DashboardWidgets
                .selectAll()
                .where { DashboardWidgets.sessionId eq sessionId }
                .count()
                .toInt()

            val newId = UUID.randomUUID()
            DashboardWidgets.insert {
                it[id] = newId
                it[DashboardWidgets.sessionId] = sessionId
                it[DashboardWidgets.chartType] = chartType
                it[DashboardWidgets.title] = title
                // We store the validated SQL, not its result -- the result is re-fetched on every
                // read (see SandboxExecutor usage in WidgetService), so widgets stay live if the
                // underlying data changes later, and there's only ever one source of truth for it.
                it[DashboardWidgets.sqlQuery] = sqlQuery
                it[DashboardWidgets.createdAt] = Instant.now()
                it[position] = nextPosition
            }

            WidgetRow(newId, chartType, title, sqlQuery, nextPosition)
        }

    suspend fun findBySession(sessionId: UUID): List<WidgetRow> = newSuspendedTransaction(Dispatchers.IO) {
        DashboardWidgets.selectAll()
            .where { DashboardWidgets.sessionId eq sessionId }
            .orderBy(DashboardWidgets.position, SortOrder.ASC)
            .map {
                WidgetRow(
                    id = it[DashboardWidgets.id],
                    chartType = it[DashboardWidgets.chartType],
                    title = it[DashboardWidgets.title],
                    sqlQuery = it[DashboardWidgets.sqlQuery],
                    position = it[DashboardWidgets.position]
                )
            }
    }
}
