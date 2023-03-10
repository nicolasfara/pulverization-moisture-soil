import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.shadowjar)
}

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }

        tasks {
            register<ShadowJar>("sensorsJar") {
                genericJarConfig(
                    "sensors",
                    "it.nicolasfarabegoli.moisture.units.SensorsUnitKt",
                )
            }
            register<ShadowJar>("actuatorsJar") {
                genericJarConfig(
                    "actuators",
                    "it.nicolasfarabegoli.moisture.units.ActuatorsUnitKt",
                )
            }
            register<ShadowJar>("behaviourJar") {
                genericJarConfig(
                    "behaviour",
                    "it.nicolasfarabegoli.moisture.units.BehaviourUnitKt",
                )
            }
            register("generateJars") { dependsOn("sensorsJar", "actuatorsJar", "behaviourJar") }
        }
    }

    // Native platforms
    val commonMain by sourceSets.getting {
        dependencies {
            implementation(libs.kotlin.stdlib)
            implementation(libs.bundles.pulverization)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization)
            implementation(libs.ktor.network)
        }
    }

    val jvmMain by sourceSets.getting {
        dependencies {
            implementation(libs.bundles.pi4j)
        }
    }

    val nativeMain by sourceSets.creating {
        dependsOn(commonMain)
    }

    linuxX64 {
        compilations["main"].defaultSourceSet.dependsOn(nativeMain)
//        binaries {
//            executable {
//                entryPoint = "it.nicolasfarabegoli.moisture.main"
//            }
//        }
    }
}

fun ShadowJar.genericJarConfig(jarName: String, mainClass: String) {
    archiveClassifier.set("all")
    archiveBaseName.set(jarName)
    archiveVersion.set("")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    exclude("META-INF/*.SF", "META-INF/*.RSA", "META-INF/*.DSA")
    manifest {
        attributes("Main-Class" to mainClass)
    }
    val main by kotlin.jvm().compilations
    from(main.output)
    configurations += main.compileDependencyFiles as Configuration
    configurations += main.runtimeDependencyFiles as Configuration
}
