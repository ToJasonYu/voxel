package com.voxel

import com.expediagroup.graphql.server.ktor.GraphQL
import com.expediagroup.graphql.server.ktor.graphQLPostRoute
import com.expediagroup.graphql.server.ktor.graphQLSubscriptionsRoute
import com.expediagroup.graphql.server.ktor.graphiQLRoute
import com.voxel.db.Database
import com.voxel.graphql.DashboardMutation
import com.voxel.graphql.DashboardQuery
import com.voxel.graphql.DashboardSubscription
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import org.slf4j.event.Level

fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
    Database.connect()

    embeddedServer(Netty, port = port, host = "0.0.0.0") {
        install(CallLogging) { level = Level.INFO }
        install(CORS) {
            // No auth on this project (see README) -- any origin can call the API.
            anyHost()
            allowHeader(HttpHeaders.ContentType)
        }
        install(WebSockets) // required by graphQLSubscriptionsRoute below

        install(GraphQL) {
            schema {
                packages = listOf("com.voxel.graphql")
                queries = listOf(DashboardQuery())
                mutations = listOf(DashboardMutation())
                subscriptions = listOf(DashboardSubscription())
            }
        }

        routing {
            get("/health") {
                call.respondText("ok")
            }
            get("/health/db") {
                if (Database.ping()) {
                    call.respondText("ok")
                } else {
                    call.respond(HttpStatusCode.ServiceUnavailable, "db unreachable")
                }
            }

            graphQLPostRoute()
            graphQLSubscriptionsRoute()
            graphiQLRoute() // browser UI at /graphiql for manually poking the API
        }
    }.start(wait = true)
}
