plugins {
    id("shared-build-conventions")
    id("application")
}

dependencies {
    implementation(project(":util"))
    implementation(project(":compiler"))
    implementation(project(":hson"))
}

application {
    applicationName = "hs"
    mainClass.set("pascal.hardscript.Main")
}

tasks.compileJava {
    options.compilerArgs.add("-Aproject=${project.group}/${project.name}")
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "pascal.hardscript.Main",
            "Multi-Release" to "true"
        )
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }) {
        exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
    }
}

val cppBuildDir = file("$projectDir/hson/cpp-build")
val cmakeSourceDir = file("$projectDir/hson")

// Configure CMake task
tasks.register("configureCMake", Exec::class) {
    group = "build"
    description = "Configures the C++ project using CMake."
    workingDir = cppBuildDir
    commandLine = listOf(
        "cmake",
        cmakeSourceDir.absolutePath,
        "-DCMAKE_BUILD_TYPE=Release",
        "-DBUILD_TESTS=OFF" // Disable tests by default
    )
    doFirst {
        cppBuildDir.mkdirs()
    }
}

// Build CMake task
tasks.register("buildCMake", Exec::class) {
    group = "build"
    description = "Builds the C++ project using CMake."
    workingDir = cppBuildDir
    commandLine = listOf("cmake", "--build", ".", "--config", "Release")
    dependsOn("configureCMake")
}

// Clean CMake build
tasks.register("cleanCMake", Exec::class) {
    group = "build"
    description = "Cleans the C++ build directory."
    workingDir = cppBuildDir
    commandLine = listOf("cmake", "--build", ".", "--target", "clean")
}

// Integrate with Gradle clean task
tasks.clean {
    dependsOn("cleanCMake")
}

// Ensure Gradle build depends on the CMake build
tasks.build {
    dependsOn("buildCMake")
}