plugins {
    `shared-build-conventions`
    application
    distribution
}

dependencies {
    implementation(project(":util"))
    implementation(project(":compiler"))
    implementation(project(":backend"))
    testImplementation(testFixtures(project(":compiler")))

    implementation("info.picocli:picocli:4.7.4")
    annotationProcessor("info.picocli:picocli-codegen:4.7.4")
}

application {
    applicationName = "nx"
    mainClass.set("pascal.netx.Main")
}

tasks.compileJava {
    options.compilerArgs.add("-Aproject=${project.group}/${project.name}")
}

tasks.jar {
    manifest {
        // Bare file names: installDist and jpackage both put every dependency in the
        // same directory as this jar, so the entries resolve at runtime and the jar
        // can also be launched directly with `java -jar`.
        val classPath = configurations.runtimeClasspath.get().joinToString(" ") { it.name }
        attributes(
            "Main-Class" to "pascal.netx.Main",
            "Multi-Release" to "true",
            "Implementation-Title" to "NetX CLI",
            "Implementation-Version" to project.version,
            "Class-Path" to classPath
        )
    }
}

val cppBuildDir = file("$projectDir/build/nxsim")
val cmakeSourceDir = file("$projectDir/simulator")

tasks.register("configureCMake", Exec::class) {
    group = "build"
    description = "Configures the C++ project using CMake."
    workingDir = cppBuildDir
    val configureArgs = mutableListOf(
        "cmake", cmakeSourceDir.absolutePath, "-DCMAKE_BUILD_TYPE=Release", "-DTESTING=OFF"
    )
    System.getenv("CC")?.takeIf { it.isNotBlank() }?.let {
        configureArgs += "-DCMAKE_C_COMPILER=$it"
    }
    System.getenv("CXX")?.takeIf { it.isNotBlank() }?.let {
        configureArgs += "-DCMAKE_CXX_COMPILER=$it"
    }
    // Escape hatch for CI/packaging, e.g. a vcpkg toolchain file or -DNXSIM_NATIVE_ARCH=ON.
    System.getenv("NXSIM_CMAKE_ARGS")?.takeIf { it.isNotBlank() }?.let {
        configureArgs += it.split(" ").filter(String::isNotBlank)
    }
    commandLine = configureArgs
    doFirst {
        cppBuildDir.mkdirs()
    }
    outputs.dir(cppBuildDir)
    inputs.dir(cmakeSourceDir)
}

tasks.register("buildCMake", Exec::class) {
    group = "build"
    description = "Builds the C++ project using CMake."
    workingDir = cppBuildDir
    commandLine = listOf("cmake", "--build", ".", "--config", "Release", "-j")
    dependsOn("configureCMake")
    inputs.dir(cmakeSourceDir)
    outputs.dir("$cppBuildDir/src")
}

tasks.register("cleanCMake", Exec::class) {
    group = "build"
    description = "Cleans the C++ build directory."
    workingDir = cppBuildDir
    commandLine = listOf("cmake", "--build", ".", "--target", "clean")
    onlyIf { cppBuildDir.exists() }
}

tasks.clean {
    dependsOn("cleanCMake")
}
