import org.ajoberstar.grgit.Grgit
import org.cadixdev.gradle.licenser.LicenseExtension

plugins {
    id("java-library")
    id("org.cadixdev.licenser") version "0.6.1"
    id("maven-publish")
    id("eclipse")
    id("idea")
    id("com.jfrog.artifactory")
    id("checkstyle")
}


logger.lifecycle("""
*******************************************
 You are building SquirrelID!

 If you encounter trouble:
 1) Read README.md if you haven't yet
 2) Try running 'build' in a separate Gradle run
 3) Use gradlew and not gradle
 4) If you still need help, ask on Discord! https://discord.gg/enginehub

 Output files will be in build/libs
*******************************************
""")

applyArtifactoryConfig()

repositories {
    mavenCentral()
    maven { url = uri("https://maven.enginehub.org/repo/") }
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }
}

configurations.all {
    resolutionStrategy {
        force("com.google.guava:guava:21.0")
    }
}

dependencies {
    "implementation"("com.google.guava:guava:${Versions.GUAVA}")
    "implementation"("com.google.code.findbugs:jsr305:1.3.9")
    "implementation"("com.googlecode.json-simple:json-simple:1.1")
    "implementation"("org.xerial:sqlite-jdbc:3.7.2")
    "implementation"("com.destroystokyo.paper:paper-api:1.13.2-R0.1-SNAPSHOT")

    "testImplementation"("org.junit.jupiter:junit-jupiter-api:${Versions.JUNIT}")
    "testImplementation"("org.junit.jupiter:junit-jupiter-params:${Versions.JUNIT}")
    "testImplementation"("org.hamcrest:hamcrest:2.2")
    "testRuntimeOnly"("org.junit.jupiter:junit-jupiter-engine:${Versions.JUNIT}")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(8))
    withSourcesJar()
    withJavadocJar()
}

configure<LicenseExtension> {
    header.set(resources.text.fromFile(file("HEADER.txt")))
    include("**/*.java")
}

if (!project.hasProperty("gitCommitHash")) {
    apply(plugin = "org.ajoberstar.grgit")
    ext["gitCommitHash"] = try {
        extensions.getByName<Grgit>("grgit").head()?.abbreviatedId
    } catch (e: Exception) {
        logger.warn("Error getting commit hash", e)

        "no.git.id"
    }
}

configure<PublishingExtension> {
    publications {
        register<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
