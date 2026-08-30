package com.voxel.graphql

import com.expediagroup.graphql.generator.scalars.ID
import com.expediagroup.graphql.server.operations.Query
import com.voxel.db.SessionRepository
import com.voxel.db.WidgetRepository
import java.util.UUID

class DashboardQuery : Query {
    suspend fun session(id: ID): DashboardSession? {
        val sessionId = UUID.fromString(id.value)
        if (!SessionRepository.exists(sessionId)) return null

        val widgets = WidgetRepository.findBySession(sessionId).map { it.toGraphQLWidget() }
        return DashboardSession(id, widgets)
    }
}
