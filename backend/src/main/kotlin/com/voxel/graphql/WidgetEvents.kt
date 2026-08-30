package com.voxel.graphql

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.UUID

/**
 * In-process pub/sub: every new widget gets published here, and the
 * dashboardUpdated subscription filters this one stream down to a single
 * session. That's the whole mechanism behind live multi-client sync -- no
 * message broker, because a single backend instance doesn't need one.
 */
object WidgetEvents {
    private val _events = MutableSharedFlow<WidgetCreated>(replay = 0, extraBufferCapacity = 16)
    val events = _events.asSharedFlow()

    suspend fun publish(sessionId: UUID, widget: Widget) {
        _events.emit(WidgetCreated(sessionId, widget))
    }
}

data class WidgetCreated(val sessionId: UUID, val widget: Widget)
