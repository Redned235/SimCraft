description = "simcraft-gui"

plugins {
    id("application")
}

dependencies {
    api(project(":common"))
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