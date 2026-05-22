plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform(libs.ktor.bom))
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.mcp.kotlin.server)
    implementation(libs.logback.classic)

    testImplementation(libs.junit.jupiter)
    testImplementation(kotlin("test"))
    testImplementation(libs.ktor.client.cio)
    testImplementation(libs.mcp.kotlin.client)
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass = "com.jacekgajek.testmcp.AppKt"
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
