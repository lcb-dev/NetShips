plugins {
    id("java")
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
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
}

javafx {
    version = "25"
    modules("javafx.controls", "javafx.fxml")
}

tasks.test {
    useJUnitPlatform()
}
