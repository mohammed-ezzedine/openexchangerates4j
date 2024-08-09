plugins {
    id("java-library")
    id("maven-publish")
    id("signing")
    id("org.springframework.boot") version "3.3.2"
    id("io.spring.dependency-management") version "1.1.6"
}

group = "me.ezzedine.mohammed"
version = "1.0.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
    withJavadocJar()
    withSourcesJar()
}

tasks.named<Jar>("jar") {
    archiveClassifier.set("")
}

tasks.named("bootJar") {
    enabled = false
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.mock-server:mockserver-netty:5.15.0")
    testImplementation("org.mock-server:mockserver-client-java:5.15.0")
    testImplementation("com.google.jimfs:jimfs:1.3.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            groupId = "me.ezzedine.mohammed"
            artifactId = "openexchangerates4j"
            version = "1.0.0"

            pom {
                name.set("Open Exchange Rates for Java")
                description.set("A java client for Open Exchange Rates built with spring boot")
                url.set("https://github.com/mohammed-ezzedine/openexchangerates4j")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://github.com/mohammed-ezzedine/openexchangerates4j/blob/main/LICENSE")
                    }
                }
                developers {
                    developer {
                        id.set("mezzedine")
                        name.set("Mohammed Ezzedine")
                        email.set("mohammed.a.ezzedine@outlook.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/mohammed-ezzedine/openexchangerates4j.git")
                    developerConnection.set("scm:git:ssh://github.com/mohammed-ezzedine/openexchangerates4j.git")
                    url.set("https://github.com/mohammed-ezzedine/openexchangerates4j")
                }
            }
        }
    }
    repositories {
        maven {
            name = "MavenCentral"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = findProperty("ossrhUsername") as String? ?: ""
                password = findProperty("ossrhPassword") as String? ?: ""
            }
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications["mavenJava"])
}

