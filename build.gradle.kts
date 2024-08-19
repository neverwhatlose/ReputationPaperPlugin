plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("java")
    //id("checkstyle")
}

group = "ru.nwtls"
version = "1.2.1-RELEASE"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/") // cloud paper command

    maven("https://repo.dmulloy2.net/repository/public/" ) //shadow gradle
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation("cloud.commandframework:cloud-paper:1.8.4")
    implementation ("mysql:mysql-connector-java:8.0.33")
    implementation("org.apache.maven.plugins:maven-checkstyle-plugin:3.4.0")

}

tasks.test {
    useJUnitPlatform()
}

/*
checkstyle {
    toolVersion = "10.14.2"
    configFile = file("${project.rootDir}/config/checkstyle/checkstyle.xml")
    configProperties["configDirectory"] = "${project.rootDir}/config/checkstyle"
    isIgnoreFailures = false
    maxWarnings = 0
    maxErrors = 0
}
 */


project.tasks.build {
    dependsOn(tasks.shadowJar)
}