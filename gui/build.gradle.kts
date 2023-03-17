description = "simcraft-gui"

plugins {
    id("application")
    id("org.openjfx.javafxplugin") version "0.0.8"
}

dependencies {
    api(project(":common"))

    implementation("com.formdev:flatlaf:3.0")
}

application {
    mainClass.set("me.redned.simcraft.SimCraftGUI")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    from("src/main/java/resources") {
        include("*")
    }

    archiveFileName.set("SimCraft.jar")
    archiveClassifier.set("")
}