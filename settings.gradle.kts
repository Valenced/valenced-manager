rootProject.name = "revenge-manager"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

include(":app")

// The Revenge patch bundle, packed into the app's assets.
includeBuild("patches")

// A local Reseam engine checkout replaces the published SDK.
(System.getenv("RESEAM_WORKSPACE") ?: providers.gradleProperty("reseam.workspace").orNull)
    ?.takeIf { it.isNotBlank() }
    ?.let { includeBuild(it) }
