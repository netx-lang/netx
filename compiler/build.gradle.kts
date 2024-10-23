plugins {
    id("shared-build-conventions")
    antlr
}

dependencies {
    antlr("org.antlr:antlr4:4.13.2")
    implementation(project(":util"))
    implementation("org.antlr:antlr4-runtime:4.13.2")
}

tasks.generateGrammarSource {
    arguments = listOf("-package", "pascal.hardscript.compiler.parser")
}