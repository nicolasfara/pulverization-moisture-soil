@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
}

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
    }

    // Native platforms
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlin.stdlib)
                implementation(libs.bundles.pulverization)
                implementation(libs.kotlinx.coroutines.core)
            }
        }

        val nativeMain by creating {
            dependsOn(commonMain)
        }
    }

    linuxX64 {
        compilations["main"].defaultSourceSet.dependsOn(sourceSets["nativeMain"])
//        binaries {
//            executable {
//                entryPoint = "it.nicolasfarabegoli.moisture.main"
//            }
//        }
    }
}
