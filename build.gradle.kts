plugins {
    id("java-library")
    id("maven-publish")
}

group = "com.github.Xisepe"
version = "1.0.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }

    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenCentral()
}

dependencies {
    // Lombok
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")

    // Caffeine
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")

    // Jackson
    implementation("com.fasterxml.jackson.core:jackson-databind:2.20.0")

    // OkHttp
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Logging API only
    api("org.slf4j:slf4j-api:2.0.9")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

repositories {
    mavenCentral()
}

tasks.named<Jar>("jar") {
    exclude("**/examples/**")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            pom {
                name.set("OpenWeatherSDK")
                description.set(
                    "OpenWeather SDK - Java library for seamless integration with OpenWeather API. " +
                            "Provides type-safe access to current weather, forecasts, and historical data with " +
                            "easy configuration and comprehensive error handling."
                )
                url.set("https://github.com/Xisepe/open-weather-sdk")

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                scm {
                    url.set("https://github.com/Xisepe/open-weather-sdk")
                    connection.set("scm:git:git://github.com/Xisepe/open-weather-sdk.git")
                    val ghToken = System.getenv("token") ?: "TOKEN_PLACEHOLDER"
                    developerConnection.set("scm:git:https://$ghToken@github.com/Xisepe/open-weather-sdk.git")
                }
            }
        }
    }

    repositories {
        mavenLocal()
    }
}

tasks.register("printPublication") {
    doLast {
        println("Group: $group, ArtifactId: ${project.name}, Version: $version")
    }
}