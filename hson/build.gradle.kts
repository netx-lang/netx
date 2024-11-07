plugins {
    id("shared-build-conventions")
}

dependencies {
    implementation(project(":util"))

    implementation("org.jgrapht:jgrapht-core:1.5.2")
    implementation("org.jgrapht:jgrapht-io:1.5.2")
}
