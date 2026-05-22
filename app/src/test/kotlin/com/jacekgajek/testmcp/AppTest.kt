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
            arguments = mapOf("query" to "Tell me about Voyager 1"),
        )
        val content = result.content.single()
        assertIs<TextContent>(content)
        assertEquals(
            "Voyager 1 (1977): Explored outer planets and became the first human-made object to enter interstellar space.",
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
            "Known missions: Apollo 11, Voyager 1, Artemis I, James Webb Space Telescope.",
            content.text,
        )
    }
}
