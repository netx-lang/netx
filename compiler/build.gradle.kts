plugins {
    `shared-build-conventions`
    `java-library`
    `java-test-fixtures`
    antlr
}

configurations.api {
    setExtendsFrom(extendsFrom.filterNot { it.name == "antlr" })
}

dependencies {
    implementation(project(":util"))
    antlr("org.antlr:antlr4:4.13.2")
    api("org.antlr:antlr4-runtime:4.13.2")
    implementation("org.jgrapht:jgrapht-core:1.5.2")

    val jgitVersion = "7.8.1.202611041320-r"
    implementation("org.eclipse.jgit:org.eclipse.jgit:$jgitVersion")
    implementation("org.eclipse.jgit:org.eclipse.jgit.ssh.apache:$jgitVersion")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-toml")

    constraints {
        val sshdVersion = "2.20.0"
        implementation("org.apache.sshd:sshd-osgi:$sshdVersion")
        implementation("org.apache.sshd:sshd-sftp:$sshdVersion")
        implementation("commons-codec:commons-codec:1.23.0")
    }

    testFixturesImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testFixturesImplementation("com.fasterxml.jackson.core:jackson-databind:2.23.0")
    testFixturesImplementation(project(":util"))
    testFixturesImplementation(project(":compiler"))
}

tasks.generateGrammarSource {
    outputDirectory = file(layout.buildDirectory.dir("generated-src/antlr/main/pascal/netx/compiler/antlr"))
    packageName = "pascal.netx.compiler.antlr"
}
