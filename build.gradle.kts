plugins {
    id("java")
    id("org.openjfx.javafxplugin") version "0.1.0"
    application
}

group = "casey.lcbdev"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // TestFX core and JUnit5 support
    testImplementation("org.testfx:testfx-core:4.0.18")
    testImplementation("org.testfx:testfx-junit5:4.0.18")
}

javafx {
    version = "25"
    modules("javafx.controls", "javafx.fxml")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

tasks.test {
    useJUnitPlatform()

    jvmArgs = listOf(
        "--module-path", classpath.asPath,
        "--add-modules", "javafx.controls,javafx.fxml",
        "--add-opens=javafx.controls/javafx.scene=ALL-UNNAMED",
        "--add-opens=javafx.graphics/javafx.scene=ALL-UNNAMED",
        "--add-opens=javafx.base/javafx.beans=ALL-UNNAMED"
    )

    systemProperty("testfx.robot", "glass")
    systemProperty("testfx.headless", "true")
    systemProperty("prism.order", "sw")
    systemProperty("prism.text", "t2k")

    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        displayGranularity = 2
    }
}




application {
    mainClass.set("casey.lcbdev.NetShips")
}