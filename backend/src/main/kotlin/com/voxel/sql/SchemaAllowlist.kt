package com.voxel.sql

/**
 * The only schema the voice-to-SQL flow is allowed to touch. Deliberately
 * excludes dashboard_sessions/dashboard_widgets -- those are app state, not
 * data for the LLM to query.
 */
object SchemaAllowlist {
    val TABLES = setOf("customers", "orders", "order_items")

    // Flat set rather than per-table columns: JSQLParser doesn't resolve which
    // table a bare column belongs to without full semantic binding, and none of
    // our column names collide across tables in a way that matters for read-only
    // access, so a single allowlist keeps this simple without weakening it.
    val COLUMNS = setOf(
        "id", "name", "signup_date", "plan_tier",
        "customer_id", "order_date", "total_amount", "status",
        "order_id", "product_name", "quantity", "unit_price"
    )

    const val MAX_ROW_LIMIT = 500
}
