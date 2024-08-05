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
    implementation(libs.compose.runtime)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.coroutines.desktop)
    implementation(project(":svgLib"))
    implementation(project(":svgComposeLib"))
}

tasks.test {
    useJUnitPlatform()
}