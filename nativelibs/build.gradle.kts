plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-library`
}

java {
    sourceSets {
        main {
            java {
                srcDir("target/java")
                srcDir("kotlinsrc/main")
                resources {
                    srcDir("target/jar")
                }
            }
        }
        test {
            kotlin {
                srcDir("kotlinsrc/test")
            }
        }
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-receivers")
    }
}

dependencies {
    implementation(project(":svgLib"))
    implementation(libs.kotlinx.coroutines)

    testImplementation(kotlin("test"))
}

abstract class BuildNative @Inject constructor() : DefaultTask() {
    @InputDirectory
    val rustSrcDir: File = project.file("src")

    @OutputDirectory
    val javaOutDir: File = project.file("target/java")

    @OutputDirectory
    val jarOutDir: File = project.file("target/jar")

    @get:Inject
    abstract val execOperations: ExecOperations

    @get:Inject
    abstract val fileSystemOperations: FileSystemOperations

    @TaskAction
    fun run() {
        execOperations.exec {
            executable("cargo")
            args("build")
        }
        fileSystemOperations.delete {
            delete("target/jar")
        }
        File("target/jar/resources").mkdirs()
        fileSystemOperations.copy {
            from("target/debug/libnativelibs.so")
            into("target/jar/resources")
        }
    }
}
tasks.register<BuildNative>("buildNative") {
}

afterEvaluate {
    tasks.named("compileKotlin") {
        dependsOn("buildNative")
    }
    tasks.named("processResources") {
        dependsOn("buildNative")
    }
}

tasks.test {
    useJUnitPlatform()
}