@file:Suppress("UnstableApiUsage")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://maven.rikka.app/releases") }
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven {
            name = "aliucord"
            url = uri("https://maven.aliucord.com/snapshots")
        }
        maven {
            name = "jitpack"
            url = uri("https://jitpack.io")
        }
    }
}

rootProject.name = "AliucordManager"
include(":app")
