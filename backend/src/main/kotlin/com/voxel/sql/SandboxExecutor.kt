package com.voxel.sql

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.sql.ResultSet

/**
 * Executes an already-validated SELECT using its own connection pool, logged in
 * as the read-only `voxel_readonly` Postgres role (see db/init/002_readonly_role.sql).
 * This is the second, database-enforced layer of defense described in the README --
 * even a bug in SqlValidator can't turn into a write or an out-of-scope read here,
 * because the database itself won't allow it.
 */
object SandboxExecutor {
    private val dbHost = System.getenv("DB_HOST") ?: "localhost"
    private val dbPort = System.getenv("DB_PORT") ?: "5432"
    private val dbName = System.getenv("DB_NAME") ?: "voxel"
    private val readonlyUser = System.getenv("READONLY_DB_USER") ?: "voxel_readonly"
    private val readonlyPassword = System.getenv("READONLY_DB_PASSWORD") ?: "voxel_readonly"
    private const val STATEMENT_TIMEOUT_MS = 5000

    private val dataSource: HikariDataSource by lazy {
        val config = HikariConfig().apply {
            // statement_timeout is a Postgres session setting passed through the JDBC URL's
            // "options" param. It kills any query -- even one that's syntactically fine but
            // pathologically slow -- that runs past this, enforced by the database itself.
            jdbcUrl = "jdbc:postgresql://$dbHost:$dbPort/$dbName" +
                "?options=-c%20statement_timeout=$STATEMENT_TIMEOUT_MS"
            username = readonlyUser
            password = readonlyPassword
            driverClassName = "org.postgresql.Driver"
            maximumPoolSize = 5
            isReadOnly = true // belt-and-suspenders: the JDBC driver itself refuses writes on this pool
        }
        HikariDataSource(config)
    }

    /** [validatedSql] must already be SqlValidator output -- never a raw LLM string. */
    fun execute(validatedSql: String): List<Map<String, Any?>> =
        dataSource.connection.use { connection ->
            connection.createStatement().use { statement ->
                statement.executeQuery(validatedSql).use { it.toRows() }
            }
        }

    private fun ResultSet.toRows(): List<Map<String, Any?>> {
        val columnCount = metaData.columnCount
        val rows = mutableListOf<Map<String, Any?>>()
        while (next()) {
            rows += (1..columnCount).associate { i -> metaData.getColumnLabel(i) to getObject(i) }
        }
        return rows
    }
}
