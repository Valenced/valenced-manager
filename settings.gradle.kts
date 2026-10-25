rootProject.name = "valenced-manager"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

include(":app")

// A local Reseam engine checkout replaces the published SDK.
(System.getenv("RESEAM_WORKSPACE") ?: providers.gradleProperty("reseam.workspace").orNull)
    ?.takeIf { it.isNotBlank() }
    ?.let { includeBuild(it) }
