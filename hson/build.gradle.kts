plugins {
    id("shared-build-conventions")
    antlr
}

dependencies {
    implementation(project(":util"))
    antlr("org.antlr:antlr4:4.13.2")
    implementation("org.antlr:antlr4-runtime:4.13.2")
    implementation("org.jgrapht:jgrapht-core:1.5.2")
    implementation("org.jgrapht:jgrapht-io:1.5.2")
}

tasks.generateGrammarSource {
    outputDirectory = file(layout.buildDirectory.dir("generated-src/antlr/main/pascal/hardscript/hson/antlr"))
    arguments = listOf("-package", "pascal.hardscript.hson.antlr")
}