plugins {
    kotlin("jvm") version "2.0.0"
    `maven-publish`
    signing
}

group = "io.github.ezrnest"
version = "0.0.1"

repositories {
    maven {
        url = uri("https://maven.aliyun.com/repository/public/")
    }
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            pom {
                name.set("MathSymK")
                description.set("A modern Kotlin library for symbolic mathematics.")
                url.set("https://github.com/ezrnest/mathsymk")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("ezrnest")
                        name.set("Ezrnest")
                        email.set("1403718476@qq.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/ezrnest/mathsymk.git")
                    developerConnection.set("scm:git:ssh://github.com:ezrnest/mathsymk.git")
                    url.set("https://github.com/Ezrnest/MathSymK")
                }
            }
        }
    }

    repositories {
        maven {
            name = "OSSRH"
            url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = project.findProperty("ossrhToken") as String? ?: ""
                password = ""
            }
        }
    }
}

signing {
    useInMemoryPgpKeys(
        project.findProperty("signing.key") as String?,
        project.findProperty("signing.password") as String?
    )
    sign(publishing.publications["mavenJava"])
}

