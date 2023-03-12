description = "simcraft-cli"

plugins {
    id("application")
}

dependencies {
    api(project(":common"))

    implementation("net.sf.jopt-simple:jopt-simple:5.0.4")
}

application {
    mainClass.set("me.redned.simcraft.Bootstrap")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    from("src/main/java/resources") {
        include("*")
    }

    archiveFileName.set("SimCraft-cli.jar")
    archiveClassifier.set("")
}