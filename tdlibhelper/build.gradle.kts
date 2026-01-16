plugins {
    id("buildsrc.convention.kotlin-jvm")

    application
}

group = "com.github.purofle"
version = "1.0-SNAPSHOT"


dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation(files("../libs/tdlib-java.jar"))

    implementation(libs.bundles.kotlinxEcosystem)
}

tasks.test {
    useJUnitPlatform()
}