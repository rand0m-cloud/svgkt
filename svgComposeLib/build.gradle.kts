plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose.compiler)
    `java-library`
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-receivers")
    }
}


dependencies {
    implementation(libs.compose.runtime)
    implementation(libs.kotlinx.coroutines)
    implementation(project(":svgLib"))
    api(project(":nativelibs"))
}

tasks.test {
    useJUnitPlatform()
}
