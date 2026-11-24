import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.vanniktechMavenPublish)
}

kotlin {
    @OptIn(ExperimentalAbiValidation::class)
    abiValidation()

    android {
        namespace = "com.kirakit.components"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }

        withHostTest {}
    }

    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            api(projects.kiraTheme)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.ui)
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.composeunstyled.primitives)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

publishing {
    repositories {
        maven {
            name = "StaticMaven"
            val stagingDir = providers.gradleProperty("staticMavenDir")
                .map { rootProject.file(it) }
                .orElse(rootProject.layout.buildDirectory.dir("maven-staging").map { it.asFile })
            url = stagingDir.get().toURI()
        }

        val customMavenUrl = providers.gradleProperty("customMavenUrl")
            .orElse(providers.environmentVariable("CUSTOM_MAVEN_URL"))
            .orNull
        if (!customMavenUrl.isNullOrBlank()) {
            maven {
                name = "CustomMaven"
                url = uri(customMavenUrl)
                val user = providers.gradleProperty("customMavenUsername")
                    .orElse(providers.environmentVariable("CUSTOM_MAVEN_USERNAME"))
                    .orNull
                val pass = providers.gradleProperty("customMavenPassword")
                    .orElse(providers.environmentVariable("CUSTOM_MAVEN_PASSWORD"))
                    .orNull
                if (!user.isNullOrBlank() && !pass.isNullOrBlank()) {
                    credentials {
                        username = user
                        password = pass
                    }
                }
            }
        }
    }
}
