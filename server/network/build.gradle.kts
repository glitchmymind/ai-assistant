plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinxSerialization)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(projects.server.core)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.call.id)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.slf4j)
    implementation(libs.logback.classic)
}
