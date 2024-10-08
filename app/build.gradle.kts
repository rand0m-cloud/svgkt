plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose.compiler)
    application
}

application {
    mainClass = "org.svgkt.app.MainKt"
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-receivers")
    }
}


dependencies {
    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.coroutines.desktop)
    implementation(libs.compose.runtime)
    implementation(project(":composeViewer"))
    implementation(project(":svgLib"))
    implementation(project(":svgComposeLib"))
}

tasks.test {
    useJUnitPlatform()
}