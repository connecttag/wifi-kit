plugins {
    alias(libs.plugins.android.library)
    `maven-publish`
}

group = "org.connecttag.libs.kotlin"
version = "1.0.0"

android {
    namespace = "org.connecttag.services.wifi"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

dependencies {
    implementation(libs.androidx.ktx)
}

publishing {
    publications {
        create<MavenPublication>("release") {
            from(components["release"])
            groupId = "org.connecttag.libs.kotlin"
            artifactId = "wifi-kit"
            version = "1.0.0"

            pom {
                name.set("ConnectTag Wifi Kit")
                description.set("Android Wi-Fi operations library supporting modern Wi-Fi Suggestions, network requests, and state monitoring")
                url.set("https://github.com/connecttag/wifi-kit")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("connecttagye")
                        name.set("Connect Tag")
                        email.set("connecttagye@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/connecttag/wifi-kit.git")
                    developerConnection.set("scm:git:ssh://git@github.com:connecttag/wifi-kit.git")
                    url.set("https://github.com/connecttag/wifi-kit/tree/main")
                }
            }
        }
    }
}
