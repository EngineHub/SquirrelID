import org.ajoberstar.grgit.Grgit
import org.cadixdev.gradle.licenser.LicenseExtension

plugins {
    id("java-library")
    id("org.cadixdev.licenser")
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

repositories {
    mavenCentral()
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
    maven { url = uri("https://maven.enginehub.org/repo/") }
}

configurations.all {
    resolutionStrategy {
        force("com.google.guava:guava:${Versions.GUAVA}")
    }
}

dependencies {
    "compileOnly"("com.google.guava:guava:${Versions.GUAVA}")
    "compileOnly"("com.google.code.findbugs:jsr305:1.3.9")
    "compileOnly"("org.xerial:sqlite-jdbc:3.36.0.3")
    "compileOnly"("io.papermc.paper:paper-api:1.19.3-R0.1-SNAPSHOT")

    "testImplementation"("com.google.guava:guava:${Versions.GUAVA}")
    "testImplementation"("com.google.code.findbugs:jsr305:1.3.9")
    "testImplementation"("org.xerial:sqlite-jdbc:3.36.0.3")
    "testImplementation"("com.googlecode.json-simple:json-simple:1.1")
    "testImplementation"("junit:junit:${Versions.JUNIT}")
    "testImplementation"("org.junit.jupiter:junit-jupiter-api:${Versions.JUPITER}")
    "testImplementation"("org.junit.jupiter:junit-jupiter-params:${Versions.JUPITER}")
    "testImplementation"("org.hamcrest:hamcrest:2.2")
    "testRuntimeOnly"("org.junit.jupiter:junit-jupiter-engine:${Versions.JUPITER}")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    withSourcesJar()
    withJavadocJar()
}

// Java 8 turns on doclint which we fail
tasks.withType<Javadoc>().configureEach {
    (options as StandardJavadocDocletOptions).apply {
        addStringOption("Xdoclint:none", "-quiet")
        tags(
            "apiNote:a:API Note:",
            "implSpec:a:Implementation Requirements:",
            "implNote:a:Implementation Note:"
        )
    }
}

configure<PublishingExtension> {
    publications {
        register<MavenPublication>("maven") {
            from(components["java"])
            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
        }
    }
}

applyArtifactoryConfig()

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
