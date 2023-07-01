plugins {
    id("java")
    id("groovy")
    id("application")
    id("org.checkerframework") version "0.6.26"
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
    implementation("com.alibaba:fastjson:2.0.32")
    implementation("com.github.mizosoft.methanol:methanol:1.7.0")
    implementation("com.opencsv:opencsv:5.7.1")

    implementation("org.apache.opennlp:opennlp-tools:2.2.0")
    implementation("cz.cuni.mff.ufal.udpipe:udpipe:1.1.0")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation(platform("org.spockframework:spock-bom:2.3-groovy-4.0"))
    testImplementation("org.spockframework:spock-core")
    testImplementation("org.junit.jupiter:junit-jupiter")
}

application {
    mainClass.set("hu.garaba.Main")
    applicationDefaultJvmArgs = listOf("--enable-preview", "-XX:+UseZGC")
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

checkerFramework {
    checkers = listOf("org.checkerframework.checker.nullness.NullnessChecker")
    extraJavacArgs = listOf("-AonlyDefs=hu.garaba.model2.*")
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("--enable-preview")
}

tasks.withType<Test> {
    jvmArgs = listOf("--enable-preview")
}

tasks.test {
    useJUnitPlatform()
}