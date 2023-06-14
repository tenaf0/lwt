plugins {
    id("java")
    id("application")
    id("org.openjfx.javafxplugin") version "0.0.13"
    id("dev.hydraulic.conveyor") version "1.5"
}

group = "hu.garaba"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains:annotations:24.0.0")

    implementation("org.xerial:sqlite-jdbc:3.42.0.0")
    implementation("org.jsoup:jsoup:1.16.1")
    implementation("com.opencsv:opencsv:5.7.1")

    implementation("org.apache.opennlp:opennlp-tools:2.2.0")
    implementation("cz.cuni.mff.ufal.udpipe:udpipe:1.1.0")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

application {
    mainClass.set("hu.garaba.Main")
    applicationDefaultJvmArgs = listOf("--enable-preview")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(19))
    }
}

javafx {
    version = "20"
    modules = listOf("javafx.controls", "javafx.fxml")
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("--enable-preview")
}

tasks.test {
    useJUnitPlatform()
}