plugins {
    id("java")
    id("groovy")
    id("application")
    id("org.checkerframework") version "0.6.33"
    id("org.openjfx.javafxplugin") version "0.0.14"
    id("dev.hydraulic.conveyor") version "1.6"

    id("com.github.ben-manes.versions") version "0.48.0"
}

group = "hu.garaba"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains:annotations:24.0.1")
    implementation("org.checkerframework:checker-util:3.38.0")

    implementation("org.apache.logging.log4j:log4j-core:3.0.0-alpha1")
    implementation("org.apache.logging.log4j:log4j-jpl:3.0.0-alpha1")

    implementation("org.xerial:sqlite-jdbc:3.43.0.0")
    implementation("org.jsoup:jsoup:1.16.1")
    implementation("com.alibaba:fastjson:2.0.40")
    implementation("com.github.mizosoft.methanol:methanol:1.7.0")
    implementation("com.opencsv:opencsv:5.8")

    implementation("org.apache.opennlp:opennlp-tools:2.3.0")
    implementation("cz.cuni.mff.ufal.udpipe:udpipe:1.1.0")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation(platform("org.spockframework:spock-bom:2.4-M1-groovy-4.0"))
    testImplementation("org.spockframework:spock-core")
    testImplementation("org.mockito:mockito-core:5.5.0")
    testImplementation("org.junit.jupiter:junit-jupiter")
}

application {
    mainClass.set("hu.garaba.Main")
    applicationDefaultJvmArgs = listOf("--enable-preview", "-XX:+UseZGC")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

javafx {
    version = "19"
    modules = listOf("javafx.controls", "javafx.fxml")
}

checkerFramework {
    checkers = listOf("org.checkerframework.checker.nullness.NullnessChecker")
    excludeTests = true
    extraJavacArgs = listOf("-AonlyDefs=hu.garaba.(model2|buffer).*", "-AskipDefs=hu.garaba.buffer.PageReader")
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("--enable-preview")
}

tasks.withType<Test> {
    jvmArgs = listOf("--enable-preview")
    systemProperties["junit.jupiter.execution.parallel.enabled"] = true
    systemProperties["junit.jupiter.execution.parallel.mode.default"] = "concurrent"
    maxParallelForks = (Runtime.getRuntime().availableProcessors() / 3).takeIf { it > 0 } ?: 1
}

tasks.test {
    useJUnitPlatform()
}
