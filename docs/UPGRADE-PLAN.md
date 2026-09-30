# Upgrade plan

## Current state

Score: 5/10 (was 4/10) — tested pure-Kotlin domain core and honest CI; the
Compose UI is still a placeholder and the Android build is unverified locally.

## Backlog

- P0: Confirm the `android` CI job is green (AGP 8.5 + Kotlin 2.0.21 Compose
  plugin + material dependency were fixed statically, without an Android SDK).
- P0: Replace the placeholder Home/Explore screens with UI backed by `core`
  (ViewModel + state holder calling the `core` API).
- P1: HTML minification (collapse inter-tag whitespace, keep `<pre>`/`<textarea>`).
- P1: Share/copy output via Android share sheet; open files via the Storage Access Framework.
- P2: Compose UI tests (`androidx.compose.ui:ui-test-junit4`) for the main flow.
- P2: Pin `distributionSha256Sum` in `gradle-wrapper.properties`.

## Done in this pass (pass 1)

- Added `core/` (included Gradle build) with the app's domain logic and 6 unit tests.
- Committed the Gradle wrapper (8.10.2).
- Fixed Kotlin 2.0 Compose setup: `org.jetbrains.kotlin.plugin.compose`
  replaces the removed `composeOptions.kotlinCompilerExtensionVersion`; Java/Kotlin
  target 17; added `com.google.android.material` for the manifest theme.
- CI: removed `|| true`; separate `core` and `android` jobs.
- README now states what is verified and what is not.

## Done in this pass (pass 2)

- Fixed CSS: the space before `:` in a selector (`div :first-child`, a descendant combinator) was dropped, changing the selector; declarations still collapse (`color : red` -> `color:red`).
- Fixed JSON: whitespace between two scalars (`[1 2]`) silently produced `[12]`; now a `MinifyException`.
- Fixed JS whitespace mode: lines inside multi-line template literals are kept verbatim (their whitespace is string content).
- 7 more edge-case tests (13 total); verified with `./gradlew -p core test --offline` (3 of them fail on the old code).
- Still not verified locally: the Compose `app/` (no Android SDK; dl.google.com is blocked here).

## Done in this pass (pass 3)

- Fixed JSON: any Kotlin `isWhitespace()` character (NBSP, U+2028, ideographic space, U+001C-U+001F) was silently dropped between tokens, turning invalid JSON into "valid" output; zero-width spaces were passed through. Only RFC 8259 whitespace is now skipped, other invisible/control characters raise `MinifyException`, and a leading BOM is tolerated.
- 2 regression tests (15 total), verified with `./gradlew -p core test --offline`.
