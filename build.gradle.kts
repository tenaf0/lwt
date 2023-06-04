plugins {
    id("java")
    id("application")
    id("org.openjfx.javafxplugin") version "0.0.13"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.xerial:sqlite-jdbc:3.42.0.0")
    implementation("org.jsoup:jsoup:1.16.1")
    implementation("org.apache.opennlp:opennlp-tools:2.2.0")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

application {
    mainClass.set("org.example.Main")
    applicationDefaultJvmArgs = listOf("--enable-preview")
}

javafx {
    version = "20"
    modules = listOf("javafx.controls")
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("--enable-preview")
}

tasks.test {
    useJUnitPlatform()
}