# Kira Kit KMP

Kira Kit V2's design tokens and UI components for Kotlin Multiplatform. The same Compose gallery runs on Android, iOS, and JVM desktop.

| Module | What goes there |
| --- | --- |
| `kira-theme` | Generated color and radius tokens, plus typography, spacing, and motion. |
| `kira-components` | Shared Compose Foundation components and Compose Unstyled primitives. Platform-specific behavior such as haptics uses `expect` and `actual`. |
| `gallery` | Shared UI Preview playground and the desktop launcher. |
| `androidApp` | Android Activity and app packaging. |
| `iosApp` | SwiftUI app that hosts the `KiraGallery` Kotlin framework. |

## Run the gallery

Set `JAVA_HOME` to JDK 17 or 21. The October 2026 toolchain uses Kotlin 2.4.21, Compose Multiplatform 1.12.2, AGP 9.5.0, and the Gradle 9.6.0 wrapper. [Kotlin releases](https://kotlinlang.org/releases.html), [Compose compatibility](https://kotlinlang.org/docs/multiplatform/compose-compatibility-and-versioning.html), [AGP 9.5 requirements](https://developer.android.com/build/releases/agp-9-5-0-release-notes)

### Android

Install Android SDK Platform 37 and Build Tools 36.0.0. Open the project root in Android Studio and run the `androidApp` configuration. To build or install from the repository root on Windows, run the corresponding command in PowerShell.

```powershell
.\gradlew.bat :androidApp:assembleDebug
.\gradlew.bat :androidApp:installDebug
```

The app compiles against API 37 to satisfy Compose and Compose Unstyled dependency metadata. It targets API 36 and supports devices running API 24 or later. The Android host is a standalone application module and shared modules use the Android Gradle library plugin for KMP. [Android KMP plugin](https://developer.android.com/kotlin/multiplatform/plugin)

### iOS

On an Apple Silicon Mac with Xcode, open `iosApp/KiraGallery.xcodeproj`. Select the **KiraGallery** scheme and an iOS simulator, then choose **Run**. For a physical device, select a development team in **Signing & Capabilities**.

The Xcode build phase runs `:gallery:embedAndSignAppleFrameworkForXcode`. The SwiftUI host loads the Compose view controller from the framework. If Xcode cannot find Java, set `JAVA_HOME` in the environment that launches Xcode. [JetBrains direct Xcode integration](https://kotlinlang.org/docs/multiplatform/multiplatform-direct-integration.html)

### Desktop and JVM tests

Run these commands from the repository root. On Windows, use `gradlew.bat` in place of `./gradlew`.

```sh
./gradlew :gallery:run
./gradlew :kira-theme:jvmTest :kira-components:jvmTest
```

## Add a component

1. Add the composable under `kira-components/src/commonMain`. Use the semantic tokens in `KiraTheme` and define the component's state and accessibility semantics.
2. Add an interactive example to the UI Preview in `gallery/src/commonMain`. Include any disabled, loading, or error states the component exposes.
3. Run the desktop gallery while iterating. Check touch, keyboard insets, and both appearances on a mobile target before publishing.

To publish the library modules to your local Maven repository, run:

```sh
./gradlew :kira-theme:publishToMavenLocal :kira-components:publishToMavenLocal
```

## Consume the library

Published releases and `development` snapshots are served as a zero-auth static Maven repository on GitHub Pages (`Kiranaide/kira-kit-kmp`):

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        maven("https://kiranaide.github.io/kira-kit-kmp/releases")
        // Optional: development branch snapshots
        maven("https://kiranaide.github.io/kira-kit-kmp/snapshots")
    }
}

// build.gradle.kts (commonMain.dependencies)
implementation("io.github.kiranaide:kira-components:0.1.0") // transitively includes kira-theme
// Or tokens only:
implementation("io.github.kiranaide:kira-theme:0.1.0")
```

## Branching, CI/CD & releases

- **`development`**: Active integration branch. PRs and pushes run full Linux (`ubuntu-24.04`) + Apple Silicon (`macos-26`) CI, Kotlin 2.4 ABI validation (`checkKotlinAbi`), publish `-SNAPSHOT` builds to `gh-pages/snapshots`, and upload testable showcase artifacts.
- **`main`**: Production release branch. Promote `development` → `main` via a regular merge commit; **Release Please** opens a Release PR bumping `gradle.properties`, `iosApp/Gallery.xcconfig`, and `CHANGELOG.md`. Merging it tags `vX.Y.Z`, publishes all KMP targets (`android`, `jvm`, `iosArm64`, `iosSimulatorArm64`) to `gh-pages/releases`, attaches showcase binaries (`.apk`, `.dmg`, `.msi`, `.deb`, `SHA256SUMS.txt`, SLSA attestations) to the GitHub Release, and automatically back-merges `main` into `development`.
- **Self-hosted / Maven Central**: Set repository variables `ENABLE_CUSTOM_MAVEN=true` + `CUSTOM_MAVEN_URL` (for Reposilite, Forgejo, Nexus, etc.) or `ENABLE_MAVEN_CENTRAL=true` (Sonatype Central Portal) with their matching secrets to publish simultaneously with zero workflow changes.
- **Public API changes**: When intentionally modifying public APIs in `:kira-theme` or `:kira-components`, run `./gradlew :kira-theme:updateKotlinAbi :kira-components:updateKotlinAbi` and commit the updated ABI dump files.

## Sync tokens from Kira Kit V2

Run `bun run export:kmp` from the `kira-kit-v2` repository. Copy the two generated files into this repository.

| Export | Destination |
| --- | --- |
| `exports/kmp/kira-tokens.json` | `tokens/kira-tokens.json` |
| `exports/kmp/KiraTheme.kt` | `kira-theme/src/commonMain/kotlin/com/kirakit/theme/KiraTheme.kt` |

