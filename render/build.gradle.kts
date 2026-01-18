plugins {
    id("buildsrc.convention.kotlin-jvm")
    id("buildsrc.convention.skiko")
    alias(libs.plugins.kotlinPluginSerialization)
}

dependencies {
    implementation(files("../libs/tdlib-java.jar"))
    implementation(project(":tdlibhelper"))
    testImplementation(kotlin("test"))
}