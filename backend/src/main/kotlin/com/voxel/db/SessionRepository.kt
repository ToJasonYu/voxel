package com.voxel.db

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.Instant
import java.util.UUID

object SessionRepository {
    // newSuspendedTransaction moves the blocking JDBC work onto Dispatchers.IO and
    // suspends the caller instead of blocking whatever thread called this -- the
    // Exposed-recommended way to use it from suspend functions like GraphQL resolvers.
    suspend fun create(): UUID {
        val newId = UUID.randomUUID()
        newSuspendedTransaction(Dispatchers.IO) {
            DashboardSessions.insert {
                it[id] = newId
                it[createdAt] = Instant.now()
            }
        }
        return newId
    }

    suspend fun exists(sessionId: UUID): Boolean = newSuspendedTransaction(Dispatchers.IO) {
        DashboardSessions.selectAll().where { DashboardSessions.id eq sessionId }.limit(1).any()
    }
}
