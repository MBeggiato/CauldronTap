# CauldronTap

[![Build](https://github.com/MBeggiato/CauldronTap/actions/workflows/build.yml/badge.svg)](https://github.com/MBeggiato/CauldronTap/actions/workflows/build.yml)

Paper plugin that fills **empty buckets** from **lava or water cauldrons** using a **dispenser**.

## How it works

1. Place a dispenser facing a **lava** or **water** cauldron.
2. Put empty buckets in the dispenser.
3. Power the dispenser — one empty bucket becomes a lava or water bucket; the cauldron is emptied.

## Requirements

- Paper **26.1.2** (or compatible)
- Java **25**

## Install

Copy the built JAR from `build/libs/` into `plugins/` and restart the server.

## Commands

| Command | Permission | Description |
|---------|------------|-------------|
| `/cauldrontap reload` | `cauldrontap.reload` (default: op) | Reload `config.yml` |

## Configuration

File: `plugins/CauldronTap/config.yml`

| Option | Default | Description |
|--------|---------|-------------|
| `debug` | `false` | Log diagnostics to the console and `plugins/CauldronTap/debug.log` |
| `filled-bucket-output` | `inventory` | Where filled buckets go: `inventory` or `chest` |
| `chest-output-position` | `back` | Chest offset when output is `chest` (see below) |

### `filled-bucket-output`

- **`inventory`** — filled bucket stays in the dispenser.
- **`chest`** — filled bucket goes into a nearby chest; falls back to `inventory` if no chest is found or the chest is full.

Works for both lava and water buckets. Accepts normal and trapped chests.

### `chest-output-position`

Only used when `filled-bucket-output: chest`. Position relative to the dispenser’s facing:

| Value | Location |
|-------|----------|
| `back` | Behind the dispenser (default) |
| `front` | In front (same side as the cauldron) |
| `down` | Below |
| `up` | Above |
| `left` / `right` | Left or right of the facing direction |

### Example

```yaml
debug: false
filled-bucket-output: chest
chest-output-position: back
```

## Build

```bash
./gradlew build
```

Output: `build/libs/CauldronTap-<version>.jar`

### CI / releases

| Workflow | Trigger | Result |
|----------|---------|--------|
| [Build](.github/workflows/build.yml) | Push to `main`, pull requests, weekly schedule | Tests, Checkstyle, SpotBugs; uploads the plugin JAR |
| [Release](.github/workflows/release.yml) | Push to `main` that changes `CHANGELOG.md`, or manual run | Bumps version, updates changelog, builds JAR, creates GitHub release |

#### Publishing a release

1. Change your code as needed.
2. Add notes under **`## [Unreleased]`** in [`CHANGELOG.md`](CHANGELOG.md) (use `### Added`, `### Changed`, `### Fixed` and bullet points).
3. Commit and push to `main` (include the `CHANGELOG.md` update).

The release workflow will:

- Bump the version automatically (**patch** by default; latest tag `v1.0.0` → `v1.0.1`)
- Move `[Unreleased]` notes into a new `## [x.y.z] - date` section
- Reset `[Unreleased]` for the next cycle
- Build `CauldronTap.jar` and create a [GitHub release](https://docs.github.com/en/repositories/releasing-projects-on-github) with those notes

**Manual release** (choose patch / minor / major): Actions → Release → Run workflow.

If `[Unreleased]` has no bullet points, the workflow skips (no accidental empty releases).

## License

GPL-3.0 — see [LICENSE](LICENSE).
