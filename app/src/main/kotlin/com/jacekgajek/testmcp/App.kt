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
    "halcyon 3" to "Halcyon 3 (2029): Joint NASA/CNSA sample-return mission to asteroid 16 Psyche using a 12 kW nuclear-electric propulsion stage. Returned 412 g of metallic regolith to the Utah Test and Training Range in 2034.",
    "drake-7" to "Drake-7 (2031): ESA interstellar precursor probe targeting the heliopause in the direction of Tau Ceti. Carries a tritium-fueled RTG cluster and a 14 m phased-array antenna; designed for a 47-year primary mission.",
    "aurora station" to "Aurora Station (2032): First permanently crewed lunar south-pole base, operated by a Roscosmos-ISRO consortium. Houses a rotating crew of six in three pressurized regolith-shielded modules at Shackleton's western rim.",
    "cassiopeia array" to "Cassiopeia Array (2033): Eight-element formation-flying X-ray interferometer stationed in a halo orbit around the Sun-Earth L5 point. Baseline of 1.2 km gives sub-microarcsecond angular resolution in the 0.5-10 keV band."
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
                    put("description", "Mission name or question, for example 'Halcyon 3'.")
                }
            },
            required = listOf("query"),
        ),
    ) { request ->
        val query = request.arguments?.get("query")?.jsonPrimitive?.content.orEmpty()
        val answer = missionResponses.entries.firstOrNull { (key, _) ->
            query.contains(key, ignoreCase = true)
        }?.value ?: "Known missions: Halcyon 3, Drake-7, Aurora Station, Cassiopeia Array."

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
