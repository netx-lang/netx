plugins {
    java
    `maven-publish`
}

group = "pascal.hardscript"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(22))
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}


dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation("com.google.code.findbugs:jsr305:3.0.2")

    implementation("org.slf4j:slf4j-api:2.0.9")
    testRuntimeOnly("org.apache.logging.log4j:log4j-slf4j2-impl:2.20.0")
    testRuntimeOnly("org.apache.logging.log4j:log4j-api:2.20.0")
    testRuntimeOnly("org.apache.logging.log4j:log4j-core:2.20.0")
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>(project.name) {
            from(components["java"])
        }
    }
}