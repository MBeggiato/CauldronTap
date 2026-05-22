# CauldronTap

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

## License

GPL-3.0 — see [LICENSE](LICENSE).
