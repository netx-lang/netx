plugins {
    `shared-build-conventions`
    `java-library`
}

dependencies {
    implementation(project(":util"))
}

tasks.test {
    maxHeapSize = "8G"
}