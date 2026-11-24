import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    android {
        namespace = "com.kirakit.gallery"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "KiraGallery"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.kiraTheme)
            implementation(projects.kiraComponents)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.ui)
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.filekit.dialogs.compose)
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
        }
    }
}

val rawVersion = providers.gradleProperty("VERSION_NAME").getOrElse("0.1.0")
val coreVersion = rawVersion.substringBefore('-')
val versionParts = coreVersion.split('.').mapNotNull { it.toIntOrNull() }
// jpackage requires numeric MAJOR.MINOR.PATCH with MAJOR >= 1 for macOS DMG and Windows MSI.
val desktopPackageVersion = when {
    versionParts.size >= 3 && versionParts[0] >= 1 ->
        "${versionParts[0]}.${versionParts[1]}.${versionParts[2]}"
    versionParts.size >= 3 ->
        "1.${versionParts[1]}.${versionParts[2]}"
    else -> "1.0.0"
}

compose.desktop {
    application {
        mainClass = "com.kirakit.gallery.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "KiraKitGallery"
            packageVersion = desktopPackageVersion
            description = "Kira Kit KMP design system and component showcase"
            vendor = "Kiranaide"

            macOS {
                bundleID = "com.kirakit.gallery"
            }

            windows {
                menuGroup = "Kira Kit"
                upgradeUuid = "7e4f8c5b-2c4d-4d6e-9c9a-3f5e8a1b2d4c"
            }

            linux {
                packageName = "kira-kit-gallery"
            }
        }
    }
}
