package com.voxel.db

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

// App state, as opposed to the seeded e-commerce data in Tables.kt: which dashboard
// sessions exist, and which widgets each one has. Mirrors db/init/001_schema.sql.

object DashboardSessions : Table("dashboard_sessions") {
    val id = uuid("id")
    val createdAt = timestamp("created_at")
    override val primaryKey = PrimaryKey(id)
}

object DashboardWidgets : Table("dashboard_widgets") {
    val id = uuid("id")
    val sessionId = uuid("session_id").references(DashboardSessions.id)
    val chartType = text("chart_type")
    val title = text("title")
    val sqlQuery = text("sql_query") // the validated query, re-run each time the widget is read -- see WidgetRepository
    val createdAt = timestamp("created_at")
    val position = integer("position")
    override val primaryKey = PrimaryKey(id)
}
