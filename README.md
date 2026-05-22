# LavaTap

A [PaperMC](https://papermc.io/) plugin for Minecraft servers.

Built on the [CrimsonWarpedcraft plugin template](https://github.com/CrimsonWarpedcraft/plugin-template) (Paper API 26.1.2, Java 25, Gradle, Shadow JAR, Checkstyle, SpotBugs).

## Requirements

- Java 25
- Paper 26.1.2 (or compatible server)

## Build

```bash
./gradlew build
```

The plugin JAR is written to `build/libs/`.

## Install

Copy `build/libs/LavaTap-<version>.jar` into your server's `plugins/` folder and restart (or use a plugin manager).

## Development

| Setting | Value |
|---------|--------|
| Project name | `LavaTap` |
| Package | `com.lavatap` |
| Main class | `com.lavatap.LavaTap` |

Configuration lives in `plugins/LavaTap/config.yml` after first run.

## Releases

| Event | Version format |
|-------|----------------|
| Push to `main` | `0.0.0-SNAPSHOT` |
| Tag `vX.Y.Z` | `X.Y.Z` |
| Tag `vX.Y.Z-RC-N` | `X.Y.Z-SNAPSHOT` |
| Pull request | `yyMMdd-HHmm-SNAPSHOT` |

Tag with semantic versioning (e.g. `v0.1.0`) to trigger a GitHub release draft.

## Repository setup

This project is independent of the upstream template. To publish your own copy:

```bash
# Create the repo on GitHub (CLI example)
gh repo create LavaTap --public --source=. --remote=origin

# Or add a remote manually
git remote add origin git@github.com:MBeggiato/LavaTap.git
git push -u origin main
```

Replace `MBeggiato/LavaTap` with your GitHub user and repository name.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).

## License

GPL-3.0 — see [LICENSE](LICENSE).
