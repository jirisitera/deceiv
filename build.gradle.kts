plugins {
    id("java")
    id("com.gradleup.shadow") version "9.6.1"
    id("xyz.jpenilla.run-paper") version "3.1.0"
}
group = "com.japicraft"
version = "INDEV"
repositories {
    mavenCentral()
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
}
dependencies {
    compileOnly("io.papermc.paper:paper-api:26.2.build.+")
    compileOnly("io.github.toxicity188:bettermodel-bukkit-api:3.4.1")
}
java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}
tasks {
    processResources {
        val props = mapOf("version" to version)
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("paper-plugin.yml") {
            expand(props)
        }
    }
    shadowJar {
        archiveBaseName = "deceiv"
        archiveClassifier = ""
        archiveVersion = ""
        manifest {
            attributes["paperweight-mappings-namespace"] = "mojang"
        }
    }
    runServer {
        minecraftVersion("26.2")
        downloadPlugins {
            modrinth("4h8rX3rt", "sbJmmsSd")
        }
    }
}
