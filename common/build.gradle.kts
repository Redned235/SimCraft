description = "simcraft-common"

dependencies {
    api("com.github.Redned235.LevelParser:anvil:master-SNAPSHOT")
    api("com.github.Redned235:SimReader:master-SNAPSHOT")

    api("org.json:json:20220924")
    api("org.cloudburstmc.math:immutable:2.0")

    val fastutilVersion = "8.5.3"
    implementation("com.nukkitx.fastutil:fastutil-long-object-maps:$fastutilVersion")

    compileOnly("org.projectlombok:lombok:1.18.24")
    annotationProcessor("org.projectlombok:lombok:1.18.24")
}