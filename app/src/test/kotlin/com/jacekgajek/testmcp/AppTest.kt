package com.jacekgajek.testmcp

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.sse.SSE
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.modelcontextprotocol.kotlin.sdk.client.Client
import io.modelcontextprotocol.kotlin.sdk.client.mcpStreamableHttp
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import kotlin.test.assertEquals
import kotlin.test.assertIs
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AppTest {
    private val server = embeddedServer(Netty, host = "127.0.0.1", port = 0) {
        mcpModule()
    }

    private val httpClient = HttpClient(CIO) {
        install(SSE)
    }

    private lateinit var mcpClient: Client

    @BeforeAll
    suspend fun beforeAll() {
        server.start(wait = false)
        val port = server.engine.resolvedConnectors().single().port
        mcpClient = httpClient.mcpStreamableHttp(url = "http://127.0.0.1:$port/mcp")
    }

    @AfterAll
    suspend fun afterAll() {
        mcpClient.close()
        httpClient.close()
        server.stop(500, 1000)
    }

    @Test
    suspend fun `space missions tool is registered`() {
        val toolNames = mcpClient.listTools().tools.map { it.name }
        assertEquals(listOf("space.missions"), toolNames)
    }

    @Test
    suspend fun `space missions tool returns mission specific response`() {
        val result = mcpClient.callTool(
            name = "space.missions",
            arguments = mapOf("query" to "Tell me about Drake-7"),
        )
        val content = result.content.single()
        assertIs<TextContent>(content)
        assertEquals(
            "Drake-7 (2031): ESA interstellar precursor probe targeting the heliopause in the direction of Tau Ceti. Carries a tritium-fueled RTG cluster and a 14 m phased-array antenna; designed for a 47-year primary mission.",
            content.text,
        )
    }

    @Test
    suspend fun `space missions tool returns fallback response`() {
        val result = mcpClient.callTool(
            name = "space.missions",
            arguments = mapOf("query" to "What about Kepler?"),
        )
        val content = result.content.single()
        assertIs<TextContent>(content)
        assertEquals(
            "Known missions: Halcyon 3, Drake-7, Aurora Station, Cassiopeia Array.",
            content.text,
        )
    }
}
