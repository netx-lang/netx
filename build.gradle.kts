plugins {
    id("shared-build-conventions")
}

dependencies {
    implementation(project(":util"))
    implementation(project(":compiler"))
    implementation(project(":hson"))

    implementation("info.picocli:picocli:4.7.4")
    annotationProcessor("info.picocli:picocli-codegen:4.7.4")
}
