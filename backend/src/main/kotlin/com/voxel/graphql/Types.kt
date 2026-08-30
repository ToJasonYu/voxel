package com.voxel.graphql

import com.expediagroup.graphql.generator.scalars.ID

// Mirrors the GraphQL schema in the README: these are plain data classes, and
// graphql-kotlin generates the actual SDL from their shape via reflection --
// there's no separate .graphqls file to keep in sync by hand.

enum class ChartType { BAR, LINE, PIE }

data class DataPoint(val label: String, val value: Double)

data class Widget(
    val id: ID,
    val chartType: ChartType,
    val title: String,
    val data: List<DataPoint>
)

data class DashboardSession(val id: ID, val widgets: List<Widget>)

data class VoiceCommandResult(
    val success: Boolean,
    val widget: Widget?,
    val errorMessage: String?
)
