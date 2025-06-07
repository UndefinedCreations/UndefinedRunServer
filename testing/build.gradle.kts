import com.undefinedcreations.nova.ServerType

plugins {
    kotlin("jvm") version "1.9.21"
    id("com.undefinedcreations.echo") version "0.0.11"
    id("com.undefinedcreations.nova")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.undefinedcreations"
version = "1.0.1"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://oss.sonatype.org/content/repositories/central")
}

dependencies {
    echo("1.21.4", mojangMappings = true, printDebug = true)
}

tasks {
    shadowJar {
        archiveFileName.set("plugin.jar")
    }
    compileKotlin {
        kotlinOptions.jvmTarget = "21"
    }
    compileJava {
        options.release.set(21)
    }
    runServer {
        serverType(ServerType.ASP)
        minecraftVersion("1.21.4")
        debugMessage(true)
        perVersionFolder(true)
        acceptMojangEula()
    }
}

java {
    disableAutoTargetJvm()
}

kotlin {
    jvmToolchain(21)
}