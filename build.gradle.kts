import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    java
    `java-library`
    idea
    id("com.gradleup.shadow") version "9.0.0"
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
    id("xyz.jpenilla.run-paper") version "2.3.0"
    id("io.freefair.lombok") version "8.13.1"
    `maven-publish`
    signing
    id("com.gradleup.nmcp.aggregation") version "1.1.0"
}

group = "io.github.pylonmc"

repositories {
    mavenCentral()
    maven("https://central.sonatype.com/repository/maven-snapshots/")
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc"
    }
    maven("https://jitpack.io") {
        name = "JitPack"
    }
    maven("https://repo.xenondevs.xyz/releases") {
        name = "InvUI"
    }
    maven("https://maven.pvphub.me/tofaa") {
        name = "EntityLib"
    }
}

val rebarVersion = project.properties["rebar.version"] as String
val minecraftVersion = project.properties["minecraft.version"] as String

dependencies {
    compileOnly("io.papermc.paper:paper-api:$minecraftVersion-R0.1-SNAPSHOT")
    compileOnly("io.github.pylonmc:rebar:$rebarVersion")

    implementation("org.bstats:bstats-bukkit:2.2.1")
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
    withSourcesJar()
    withJavadocJar()
}

tasks.shadowJar {
    mergeServiceFiles()

    archiveBaseName = project.name
    archiveClassifier = null

    relocate("org.bstats", "io.github.pylonmc.pylon.bstats")
}

bukkit {
    name = "Pylon"
    main = "io.github.pylonmc.pylon.Pylon"
    version = project.version.toString()
    apiVersion = minecraftVersion
    depend = listOf("Rebar")
    load = BukkitPluginDescription.PluginLoadOrder.STARTUP
}

tasks.runServer {
    downloadPlugins {
        github("pylonmc", "rebar", rebarVersion, "rebar-$rebarVersion.jar")
    }
    maxHeapSize = "4G"
    minecraftVersion(minecraftVersion)
}

tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = project.name

            from(components["java"])

            pom {
                name = project.name
                description = "The base addon for Rebar."
                url = "https://github.com/pylonmc/pylon"
                licenses {
                    license {
                        name = "GNU Lesser General Public License Version 3"
                        url = "https://www.gnu.org/licenses/lgpl-3.0.txt"
                    }
                }
                developers {
                    developer {
                        id = "PylonMC"
                        name = "PylonMC"
                        organizationUrl = "https://github.com/pylonmc"
                    }
                }
                scm {
                    connection = "scm:git:git://github.com/pylonmc/pylon.git"
                    developerConnection = "scm:git:ssh://github.com:pylonmc/pylon.git"
                    url = "https://github.com/pylonmc/pylon"
                }
            }
        }
    }
}

signing {
    useInMemoryPgpKeys(System.getenv("SIGNING_KEY"), System.getenv("SIGNING_PASSWORD"))

    sign(publishing.publications["maven"])
}

tasks.withType(Sign::class) {
    onlyIf {
        System.getenv("SIGNING_KEY") != null && System.getenv("SIGNING_PASSWORD") != null
    }
}

nmcpAggregation {
    centralPortal {
        username = System.getenv("SONATYPE_USERNAME")
        password = System.getenv("SONATYPE_PASSWORD")
        publishingType = "AUTOMATIC"
    }
    publishAllProjectsProbablyBreakingProjectIsolation()
}
