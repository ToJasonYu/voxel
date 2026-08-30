package com.voxel.graphql

import com.expediagroup.graphql.generator.scalars.ID
import com.expediagroup.graphql.server.operations.Subscription
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import java.util.UUID

class DashboardSubscription : Subscription {
    // Returning a Flow is graphql-kotlin's subscription contract: the framework
    // collects it and pushes each emitted Widget to the client over the
    // subscription's WebSocket as it arrives -- there's no polling involved.
    fun dashboardUpdated(sessionId: ID): Flow<Widget> {
        val targetSessionId = UUID.fromString(sessionId.value)
        return WidgetEvents.events
            .filter { it.sessionId == targetSessionId }
            .map { it.widget }
    }
}
