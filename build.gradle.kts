plugins {
    java
    `maven-publish`
}

group = "dev.tako"
version = "1.0.0"

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
    withSourcesJar()
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")
    compileOnly("net.momirealms:craft-engine-core:26.9.1")
    compileOnly("net.momirealms:craft-engine-bukkit:26.9.1")

    testImplementation("org.junit.jupiter:junit-jupiter:5.12.2")
    testImplementation("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")
    testImplementation("net.momirealms:craft-engine-core:26.9.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks {
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-Xlint:deprecation")
    }
    test { useJUnitPlatform() }
    jar { archiveFileName.set("Libuid-${project.version}.jar") }
    processResources {
        val properties = mapOf("version" to project.version.toString())
        inputs.properties(properties)
        filesMatching("paper-plugin.yml") { expand(properties) }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "libuid"
            from(components["java"])
        }
    }
}
