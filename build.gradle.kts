import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("application")
    id("java")
    id("java-library")
    id("maven-publish")
    id("com.github.johnrengelman.shadow") version "7.1.0"
}

group = "me.redned"
version = "0.1-SNAPSHOT"

java.sourceCompatibility = JavaVersion.VERSION_17
java.targetCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()

    maven("https://jitpack.io")

    maven("https://repo.opencollab.dev/maven-releases/")
    maven("https://repo.opencollab.dev/maven-snapshots/")
}

dependencies {
    api("com.github.Redned235.LevelParser:anvil:master-SNAPSHOT")
    api("com.github.Redned235:SimReader:master-SNAPSHOT")

    implementation("org.json:json:20220924")
    implementation("net.sf.jopt-simple:jopt-simple:5.0.4")
    implementation("org.cloudburstmc.math:immutable:2.0")

    val fastutilVersion = "8.5.3"
    implementation("com.nukkitx.fastutil:fastutil-long-object-maps:$fastutilVersion")

    compileOnly("org.projectlombok:lombok:1.18.24")
    annotationProcessor("org.projectlombok:lombok:1.18.24")
}

application {
    mainClass.set("me.redned.simcraft.Bootstrap")
}

tasks.withType<ShadowJar> {
    from("src/main/java/resources") {
        include("*")
    }

    archiveFileName.set("${project.name}.jar")
    archiveClassifier.set("")
}

tasks.jar {
    archiveClassifier.set("unshaded")
}

tasks.named("build") {
    dependsOn(tasks.shadowJar)
}

publishing {
    publications.create<MavenPublication>("library") {
        artifact(tasks.shadowJar)
    }
}