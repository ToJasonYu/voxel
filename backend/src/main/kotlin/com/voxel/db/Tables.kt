package com.voxel.db

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date

// Exposed table definitions mirroring db/init/001_schema.sql. Kept as their own
// file since the seed script, the query allowlist, and widget execution all need them.

object Customers : Table("customers") {
    val id = integer("id").autoIncrement()
    val name = text("name")
    val signupDate = date("signup_date")
    val planTier = text("plan_tier")
    override val primaryKey = PrimaryKey(id)
}

object Orders : Table("orders") {
    val id = integer("id").autoIncrement()
    val customerId = integer("customer_id").references(Customers.id)
    val orderDate = date("order_date")
    val totalAmount = decimal("total_amount", 10, 2)
    val status = text("status")
    override val primaryKey = PrimaryKey(id)
}

object OrderItems : Table("order_items") {
    val id = integer("id").autoIncrement()
    val orderId = integer("order_id").references(Orders.id)
    val productName = text("product_name")
    val quantity = integer("quantity")
    val unitPrice = decimal("unit_price", 10, 2)
    override val primaryKey = PrimaryKey(id)
}
