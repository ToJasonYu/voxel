package com.voxel

import com.voxel.db.Database
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
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
        }
    }.start(wait = true)
}
