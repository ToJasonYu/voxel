package com.voxel.db.seed

import com.voxel.db.Customers
import com.voxel.db.Database
import com.voxel.db.OrderItems
import com.voxel.db.Orders
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.random.Random

// Everything tunable lives up here rather than buried in the generation logic below.
private val random = Random(42) // fixed seed -> reproducible dataset across re-runs
private const val CUSTOMER_COUNT = 180
private const val MONTHS_OF_HISTORY = 18L
private val PLAN_TIERS = listOf("free", "pro", "enterprise")
private val ORDER_STATUSES = listOf("pending", "completed", "cancelled", "refunded")
private val FIRST_NAMES = listOf(
    "Ava", "Liam", "Noah", "Emma", "Olivia", "Mason", "Sophia", "Lucas", "Mia", "Ethan",
    "Isabella", "Aiden", "Amelia", "Logan", "Harper", "Elijah", "Evelyn", "James", "Abigail", "Benjamin"
)
private val LAST_NAMES = listOf(
    "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis", "Rodriguez", "Martinez",
    "Lee", "Walker", "Hall", "Young", "King", "Wright", "Scott", "Green", "Baker", "Adams"
)
private val PRODUCTS = listOf(
    "Wireless Mouse" to BigDecimal("24.99"),
    "Mechanical Keyboard" to BigDecimal("89.99"),
    "USB-C Hub" to BigDecimal("34.50"),
    "Laptop Stand" to BigDecimal("45.00"),
    "Noise Cancelling Headphones" to BigDecimal("179.99"),
    "Webcam 1080p" to BigDecimal("59.99"),
    "Desk Lamp" to BigDecimal("29.99"),
    "Monitor Arm" to BigDecimal("65.00"),
    "Portable SSD 1TB" to BigDecimal("99.99"),
    "Bluetooth Speaker" to BigDecimal("39.99")
)

/** Standalone entry point (`./gradlew seed`) that fills the seeded tables with fake data. */
fun main() {
    Database.connect()
    transaction {
        // Safe to re-run: clear old rows first instead of erroring on a second pass.
        OrderItems.deleteAll()
        Orders.deleteAll()
        Customers.deleteAll()

        val customerSignups = seedCustomers()
        seedOrdersAndItems(customerSignups)
    }
    println("Seeded $CUSTOMER_COUNT customers.")
}

private fun seedCustomers(): List<Pair<Int, LocalDate>> =
    (1..CUSTOMER_COUNT).map {
        val signupDate = randomDateBetween(LocalDate.now().minusMonths(MONTHS_OF_HISTORY), LocalDate.now())
        val name = "${FIRST_NAMES.random(random)} ${LAST_NAMES.random(random)}"
        val id = Customers.insert {
            it[Customers.name] = name
            it[Customers.signupDate] = signupDate
            it[Customers.planTier] = PLAN_TIERS.random(random)
        } get Customers.id
        id to signupDate
    }

private fun seedOrdersAndItems(customers: List<Pair<Int, LocalDate>>) {
    for ((customerId, signupDate) in customers) {
        val orderCount = random.nextInt(0, 6) // some customers never order; most place a handful
        repeat(orderCount) {
            val orderDate = randomDateBetween(signupDate, LocalDate.now())
            val items = List(random.nextInt(1, 5)) { PRODUCTS.random(random) to random.nextInt(1, 4) }
            val total = items.sumOf { (product, quantity) -> product.second.multiply(BigDecimal(quantity)) }

            val orderId = Orders.insert {
                it[Orders.customerId] = customerId
                it[Orders.orderDate] = orderDate
                it[Orders.totalAmount] = total
                it[Orders.status] = ORDER_STATUSES.random(random)
            } get Orders.id

            items.forEach { (product, quantity) ->
                OrderItems.insert {
                    it[OrderItems.orderId] = orderId
                    it[OrderItems.productName] = product.first
                    it[OrderItems.quantity] = quantity
                    it[OrderItems.unitPrice] = product.second
                }
            }
        }
    }
}

private fun randomDateBetween(start: LocalDate, end: LocalDate): LocalDate {
    val dayRange = ChronoUnit.DAYS.between(start, end).coerceAtLeast(1)
    return start.plusDays(random.nextLong(dayRange))
}
