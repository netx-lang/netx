plugins {
    `shared-build-conventions`
    `java-library`
    antlr
}

dependencies {
    implementation(project(":util"))
    antlr("org.antlr:antlr4:4.13.2")
    implementation("org.antlr:antlr4-runtime:4.13.2")
    implementation("org.jgrapht:jgrapht-core:1.5.2")
    implementation("org.jgrapht:jgrapht-io:1.5.2")

    implementation("org.eclipse.jgit:org.eclipse.jgit:7.1.1.202505221757-r")
    implementation("org.eclipse.jgit:org.eclipse.jgit.ssh.apache:7.1.1.202505221757-r")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-toml:2.15.2")
}

tasks.generateGrammarSource {
    outputDirectory = file(layout.buildDirectory.dir("generated-src/antlr/main/pascal/netx/compiler/antlr"))
    packageName = "pascal.netx.compiler.antlr"
}