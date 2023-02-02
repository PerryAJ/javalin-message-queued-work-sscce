
plugins {
    application
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    // This dependency is used by the application.
    implementation(libs.javalin)
    implementation(libs.slf4j.simple)
    implementation(libs.commonsLang)
    implementation(libs.moshi)
    implementation(libs.artemisServer)
    implementation(libs.artemisCoreClient)
}

application {
    // Define the main class for the application.
    mainClass.set("sscce.javalin.async.App")
}
