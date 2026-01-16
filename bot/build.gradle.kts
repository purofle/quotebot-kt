plugins {
    id("buildsrc.convention.kotlin-jvm")
    id("buildsrc.convention.skiko")

    application
}

dependencies {

    implementation(libs.bundles.kotlinxEcosystem)

    implementation(files("../libs/tdlib-java.jar"))

    implementation(project(":render"))
    implementation(project(":tdlibhelper"))
    implementation(kotlin("stdlib-jdk8"))

    implementation(libs.telegramBotsLongpolling)
    implementation(libs.telegramBotsClient)

    implementation(libs.slf4jApi)
    implementation(libs.kotlinLogging)
    implementation(libs.logbackClassic)


}

application {
    mainClass = "com.github.purofle.quotebot.MainKt"
}

kotlin {
    jvmToolchain(21)
}