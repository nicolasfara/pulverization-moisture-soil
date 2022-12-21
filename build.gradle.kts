@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.multiplatform)
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
    linuxX64 {
        compilations["main"].defaultSourceSet.dependsOn(sourceSets["commonMain"])
//        binaries {
//            executable {
//                entryPoint = "it.nicolasfarabegoli.moisture.main"
//            }
//        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlin.stdlib)
                implementation(libs.bundles.pulverization)
            }
        }

        val nativeMain by creating {
            dependsOn(commonMain)
        }
    }
}
