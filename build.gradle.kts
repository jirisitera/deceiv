plugins {
    id("java")
    id("com.gradleup.shadow") version "9.6.1"
    id("xyz.jpenilla.run-paper") version "3.0.2"
}
group = "com.japicraft"
version = "INDEV"
repositories {
    mavenCentral()
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
}
dependencies {
    compileOnly("io.papermc.paper:paper-api:26.2.build.+")
    compileOnly("io.github.toxicity188:bettermodel-bukkit-api:3.3.0")
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
            url("https://cdn.modrinth.com/data/4h8rX3rt/versions/lMsPP60H/bettermodel-3.3.0-paper.jar")
        }
    }
}
