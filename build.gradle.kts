plugins {
    id("shared-build-conventions")
    id("application")
}

dependencies {
    implementation(project(":util"))
    implementation(project(":compiler"))
    implementation(project(":verilog"))
    implementation(project(":simulator"))

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
    dependsOn(":util:jar")
    dependsOn(":verilog:jar")
    dependsOn(":simulator:jar")
    dependsOn(":compiler:jar")
    manifest {
        attributes(
            "Main-Class" to "pascal.netx.Main",
            "Multi-Release" to "true"
        )
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }) {
        exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
    }
}

val cppBuildDir = file("$projectDir/simulator/cpp-build")
val cmakeSourceDir = file("$projectDir/simulator")

tasks.register("configureCMake", Exec::class) {
    group = "build"
    description = "Configures the C++ project using CMake."
    workingDir = cppBuildDir
    commandLine = listOf(
        "cmake",
        cmakeSourceDir.absolutePath,
        "-DCMAKE_BUILD_TYPE=Release",
        "-DBUILD_TESTS=OFF"
    )
    doFirst {
        cppBuildDir.mkdirs()
    }
}

tasks.register("buildCMake", Exec::class) {
    group = "build"
    description = "Builds the C++ project using CMake."
    workingDir = cppBuildDir
    commandLine = listOf("cmake", "--build", ".", "--config", "Release")
    dependsOn("configureCMake")
}

tasks.register("cleanCMake", Exec::class) {
    group = "build"
    description = "Cleans the C++ build directory."
    workingDir = cppBuildDir
    commandLine = listOf("cmake", "--build", ".", "--target", "clean")
}

tasks.clean {
    dependsOn("cleanCMake")
}

tasks.build {
    dependsOn("buildCMake")
}