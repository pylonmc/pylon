import kotlin.io.path.Path
import kotlin.io.path.exists

run {
    val corePath = Path("core").toAbsolutePath()
    if (!corePath.exists()) {
        Runtime.getRuntime().exec(arrayOf("git", "clone", "https://github.com/pylonmc/pylon-core", corePath.toString())).waitFor()
    }
}

run {
    val basePath = Path("base").toAbsolutePath()
    if (!basePath.exists()) {
        Runtime.getRuntime().exec(arrayOf("git", "clone", "https://github.com/pylonmc/pylon-base", basePath.toString())).waitFor()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.4.0"
}

rootProject.name = "pylon"
includeBuild("core")
includeBuild("base")
