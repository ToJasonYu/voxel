package com.voxel.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.Database as ExposedDatabase

/**
 * Owns the app's single connection pool. Exposed itself doesn't pool connections --
 * it just wants a javax.sql.DataSource, so HikariCP sits underneath and handles
 * pooling, timeouts, and reconnects.
 */
object Database {
    private val dbHost = System.getenv("DB_HOST") ?: "localhost"
    private val dbPort = System.getenv("DB_PORT") ?: "5432"
    private val dbName = System.getenv("DB_NAME") ?: "voxel"
    private val dbUser = System.getenv("DB_USER") ?: "voxel"
    private val dbPassword = System.getenv("DB_PASSWORD") ?: "voxel"

    fun connect() {
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = "jdbc:postgresql://$dbHost:$dbPort/$dbName"
            username = dbUser
            password = dbPassword
            driverClassName = "org.postgresql.Driver"
            maximumPoolSize = 10
        }
        ExposedDatabase.connect(HikariDataSource(hikariConfig))
    }

    /** Cheap connectivity check used by the /health/db route. */
    fun ping(): Boolean = try {
        transaction { exec("SELECT 1") }
        true
    } catch (e: Exception) {
        false
    }
}
