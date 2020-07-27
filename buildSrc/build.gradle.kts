import java.util.Properties

plugins {
    `kotlin-dsl`
    kotlin("jvm") version embeddedKotlinVersion
}

repositories {
    jcenter()
    gradlePluginPortal()
    maven {
        name = "EngineHub Repository"
        url = uri("https://maven.enginehub.org/repo/")
    }
}

configurations.all {
    resolutionStrategy {
        // Fabric needs this.
        force(
            "commons-io:commons-io:2.6",
            "org.ow2.asm:asm:8.0.1",
            "org.ow2.asm:asm-commons:8.0.1"
        )
    }
}

val properties = Properties().also { props ->
    project.projectDir.resolveSibling("gradle.properties").bufferedReader().use {
        props.load(it)
    }
}

dependencies {
    implementation(gradleApi())
    implementation("gradle.plugin.net.minecrell:licenser:0.4.1")
    implementation("org.ajoberstar.grgit:grgit-gradle:4.0.2")
    implementation("org.jfrog.buildinfo:build-info-extractor-gradle:4.16.0")
}
