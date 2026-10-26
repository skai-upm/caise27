# Design

## Context

`App.run(args, stdin, out, dataDir)` already builds every repository from a single `dataDir` (`users.txt`, `groups.txt`, `tickets.txt`, `grouptickets.txt`, `attractions.txt`). `main` passes `data/`. The first argument is currently taken as the command file.

## Goals / Non-Goals

**Goals:** isolated per-run data directory chosen at start-up; guaranteed cleanup; no repository changes.

**Non-Goals:** copying permanent data into the temporary directory; Singleton/dependency-injection refactoring (Delivery 3).

## Decisions

- **Option parsing in `App`**: arguments starting with `--` are options (`--fly` only); the first other argument is the command file. Unknown options produce `ERROR: unknown option ...` and the run stops.
- **`TemporaryDataDirectory`** helper: `create(Path root, LocalDateTime now)` makes `root/yyyyMMdd_HHmmss` (adding `_1`, `_2`… when it already exists) with `Files.createDirectories`; `delete()` removes the tree bottom-up with `Files.walk` sorted in reverse order. The timestamp uses the real clock because it only names the directory; it is not a business rule (business rules keep using `SystemDate`).
- **Data-directory selection**: `run(args, stdin, out, dataDir, tmpRoot)` replaces `dataDir` with the temporary directory when `--fly` is present and wraps execution in `try/finally` so the directory is deleted on `EXIT`, end of input and errors. `main` also registers a JVM shutdown hook that deletes it if the process is interrupted (Ctrl+C); deleting an already removed directory is a no-op.
- **Same repositories**: the rest of `run` is untouched; only the `Path` passed to the repositories changes.
- **Empty start**: the directory is new and empty, so fly runs start without permanent data (isolation).
- **Build**: `<finalName>parque</finalName>` in `pom.xml`; `tmp/` added to `.gitignore`.

## Risks / Trade-offs

- [Two runs in the same second] → numeric suffix keeps names unique.
- [`kill -9` cannot be intercepted] → directory may remain under `tmp/`, which is git-ignored.
