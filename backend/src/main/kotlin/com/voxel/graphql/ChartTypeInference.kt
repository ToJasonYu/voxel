package com.voxel.graphql

// Deliberately not the LLM's job: LlmSqlGenerator's only output is a SQL string
// (see its rules), so "what kind of chart did they ask for" is decided here
// instead, with plain keyword matching against the transcript's own wording.
fun inferChartType(transcript: String): ChartType {
    val lower = transcript.lowercase()
    return when {
        "pie" in lower -> ChartType.PIE
        "line" in lower -> ChartType.LINE
        else -> ChartType.BAR
    }
}

fun inferTitle(transcript: String): String =
    transcript.trim().replaceFirstChar { it.uppercase() }
