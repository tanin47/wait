import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jreleaser.model.Active
import org.jreleaser.model.Signing.Mode

plugins {
    `java-library`
    application
    `maven-publish`
    jacoco
    id("org.jreleaser") version "1.21.0"
    id("com.gradleup.shadow") version "9.2.2"
}

group = "tanin.wait"
version = "0.2.2"

description = "Wait"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withSourcesJar()
    withJavadocJar()
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report

    reports {
        xml.required = true
        csv.required = false
        html.outputLocation = layout.buildDirectory.dir("jacocoHtml")
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.renomad:minum:8.3.2")
    implementation("com.eclipsesource.minimal-json:minimal-json:0.9.5")

    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.seleniumhq.selenium:selenium-java:4.36.0")
}

tasks.named<Test>("test") {
    useJUnitPlatform()

    maxHeapSize = "1G"

    testLogging {
        events("started", "passed", "skipped", "failed")
        showStandardStreams = true
        showStackTraces = true
        showExceptions = true
        showCauses = true
        exceptionFormat = TestExceptionFormat.FULL
    }

}

application {
    mainClass.set("tanin.wait.Main")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "io.github.tanin47"
            artifactId = "wait"
            version = project.version.toString()
            artifact(tasks.shadowJar)
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])

            pom {
                name.set("Wait")
                description.set("A wait list application")
                url.set("https://github.com/tanin47/wait")
                inceptionYear.set("2025")
                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://spdx.org/licenses/MIT.html")
                    }
                }
                developers {
                    developer {
                        id.set("tanin47")
                        name.set("Tanin Na Nakorn")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/tanin47/wait.git")
                    developerConnection.set("scm:git:ssh://github.com/tanin47/wait.git")
                    url.set("http://github.com/tanin47/wait")
                }
            }
        }
    }

    repositories {
        maven {
            url = uri(layout.buildDirectory.dir("staging-deploy"))
        }
    }
}

jreleaser {
    signing {
        active = Active.ALWAYS
        armored = true
        mode = if (System.getenv("CI") != null) Mode.MEMORY else Mode.COMMAND
        command {
            executable = "/opt/homebrew/bin/gpg"
        }
    }
    deploy {
        maven {
            mavenCentral {
                create("sonatype") {
                    setActive("ALWAYS")
                    url = "https://central.sonatype.com/api/v1/publisher"
                    stagingRepository("build/staging-deploy")
                }
            }
        }
    }
}


tasks.shadowJar {
    archiveClassifier.set("") // Remove the suffix -all.
    relocate("com", "tanin.wait.com")
    exclude("META-INF/MANIFEST.MF")
}

tasks.jar {
    manifest.attributes["Main-Class"] = "tanin.wait.Main"
}

// For CI validation.
tasks.register("printVersion") {
    doLast {
        print("$version")
    }
}
