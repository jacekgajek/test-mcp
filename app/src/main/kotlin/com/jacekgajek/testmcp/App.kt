package com.jacekgajek.testmcp

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.mcpStreamableHttp
import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import io.modelcontextprotocol.kotlin.sdk.types.McpJson
import io.modelcontextprotocol.kotlin.sdk.types.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

private val missionResponses = linkedMapOf(
    "apollo 11" to "Apollo 11 (1969): First crewed Moon landing by NASA. Neil Armstrong and Buzz Aldrin walked on the lunar surface.",
    "voyager 1" to "Voyager 1 (1977): Explored outer planets and became the first human-made object to enter interstellar space.",
    "artemis i" to "Artemis I (2022): Uncrewed NASA mission that validated Orion and SLS for future crewed lunar missions.",
    "james webb" to "James Webb Space Telescope (2021): Infrared observatory delivering deep-space observations and early-universe data."
)

fun Application.mcpModule() {
    install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
        json(McpJson)
    }

    mcpStreamableHttp {
        createServer()
    }
}

private fun createServer(): Server {
    val server = Server(
        serverInfo = Implementation(name = "space-missions-server", version = "1.0.0"),
        options = ServerOptions(
            capabilities = ServerCapabilities(
                tools = ServerCapabilities.Tools(listChanged = false),
            ),
        ),
    )

    server.addTool(
        name = "space.missions",
        description = "Returns static information about major space exploration missions.",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("query") {
                    put("type", "string")
                    put("description", "Mission name or question, for example 'Apollo 11'.")
                }
            },
            required = listOf("query"),
        ),
    ) { request ->
        val query = request.arguments?.get("query")?.jsonPrimitive?.content.orEmpty()
        val answer = missionResponses.entries.firstOrNull { (key, _) ->
            query.contains(key, ignoreCase = true)
        }?.value ?: "Known missions: Apollo 11, Voyager 1, Artemis I, James Webb Space Telescope."

        CallToolResult(content = listOf(TextContent(answer)))
    }

    return server
}

fun main(vararg args: String) {
    val port = args.firstOrNull()?.toIntOrNull() ?: System.getenv("PORT")?.toIntOrNull() ?: 3000
    embeddedServer(Netty, host = "0.0.0.0", port = port) {
        mcpModule()
    }.start(wait = true)
}
