plugins {
    id("java")
    id("com.gradleup.shadow") version "9.6.1"
    id("xyz.jpenilla.run-paper") version "3.0.2"
}
group = "com.japicraft"
version = "1.0.0"
repositories {
    mavenCentral()
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
}
dependencies {
    compileOnly("io.papermc.paper:paper-api:26.2.build.+")
}
java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}
tasks {
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
        }
    }
}
