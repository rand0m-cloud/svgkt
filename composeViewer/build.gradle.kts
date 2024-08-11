plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
    `java-library`
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-receivers")
    }
}


dependencies {
    implementation(libs.kotlinx.coroutines)

    implementation(compose.runtime)
    implementation(compose.ui)
    implementation(compose.foundation)
    implementation(compose.material)
    implementation(compose.desktop.currentOs)

    implementation(project(":svgLib"))
    implementation(project(":svgComposeLib"))
    implementation(project(":nativelibs"))
}

tasks.test {
    useJUnitPlatform()
}
