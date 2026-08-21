plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.ktor)
    application
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("com.aiassistant.server.gateway.ApplicationKt")
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    implementation(projects.server.core)
    implementation(projects.server.di)
    implementation(projects.server.application)
    implementation(projects.server.db)
    implementation(projects.server.network)
    implementation(projects.server.agent)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.call.id)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.koin.ktor)
    implementation(libs.logback.classic)

    testImplementation(kotlin("test"))
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.ktor.client.content.negotiation)
}
