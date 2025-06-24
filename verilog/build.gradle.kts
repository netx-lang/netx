plugins {
    `shared-build-conventions`
    `java-library`
}

dependencies {
    implementation(project(":util"))
    implementation("org.jgrapht:jgrapht-core:1.5.2")
    implementation("org.jgrapht:jgrapht-io:1.5.2")
}