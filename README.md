# Minifier — Mobile

Jetpack Compose Android app for **Minifier** — shrink JSON, CSS and JavaScript on the device.

Part of [Chaowalit Greepoke](https://bookchaowalit.com)'s 101 Portfolio Projects.

## Status

- `core/` — pure-Kotlin domain logic with unit tests. Builds and tests
  without the Android SDK. **This is the verified part of the repo.**
- `app/` — Compose UI shell (Home / Explore / Profile tabs). It depends on
  `core`, but the screens do not use it yet, and the Android build has so far
  only been checked in CI, not locally. See [docs/UPGRADE-PLAN.md](docs/UPGRADE-PLAN.md).

## Core features (`core/`, package `com.bookchaowalit.minifier`)

- JSON minification that preserves string contents (escaped quotes included) and rejects unbalanced brackets / unterminated strings
- CSS minification: strips comments, collapses whitespace, tightens around `{ } : ; , >`, drops the trailing `;` before `}`, keeps quoted strings verbatim
- Conservative JavaScript whitespace shrink (trim lines, drop blank lines — never rewrites tokens)
- `MinifyResult` with UTF-8 byte sizes, bytes saved and percentage saved

## Tech Stack

- **UI:** Jetpack Compose + Material 3 (Compose BOM 2024.06)
- **Language:** Kotlin 2.0 (Compose compiler Gradle plugin)
- **Android:** AGP 8.5, min SDK 26, target SDK 34
- **Build:** Gradle 8.10 wrapper (committed); `core` is an included build

## Getting Started

Requires JDK 17+.

```bash
./gradlew -p core build          # domain logic + unit tests, no Android SDK needed
./gradlew :app:assembleDebug     # needs the Android SDK (ANDROID_HOME or local.properties)
```

CI (`.github/workflows/build.yml`) runs both; either failing fails the workflow.

## Related

- **Frontend:** [bookchaowalit-website/minifier-frontend](https://github.com/bookchaowalit-website/bookchaowalit-minifier-frontend)
- **Portfolio:** [bookchaowalit.com](https://bookchaowalit.com)

## License

MIT
