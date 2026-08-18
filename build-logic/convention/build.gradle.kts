plugins {
    `kotlin-dsl`
}

group = "com.aiassistant.gradle"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    compileOnly(libs.kotlin.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("kmpDependencies") {
            id = "com.aiassistant.kmp.dependencies"
            implementationClass = "com.aiassistant.gradle.KmpDependenciesPlugin"
        }
    }
}
