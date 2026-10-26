# Design

## Context

All features are implemented: layered packages, singleton controllers configured in `App.initialize`, file repositories, `--fly`, help catalog, BCrypt, passes, generic attraction types, cascade deletion. `App.run(args, stdin, out, dataDir, tmpRoot)` is the testable entry point.

## Goals / Non-Goals

**Goals:** executable evidence that the whole delivery works together; single reference script reusable by people and tests.

**Non-Goals:** new features or syntax changes.

## Decisions

- **Reference script** `examples/input_fase3.txt`: plain command lines (comments are not part of the command language, so the file contains only commands). It ends with `EXIT` followed by a command that must not run. Dates are set explicitly with `SYSTEM DATE`, so the script is independent of the real date.
- **`FullIntegrationTest`** (presentation test package): runs the script via `App.run` as file, as stdin and with `--fly`; asserts identical outputs, the absence of unexpected `ERROR` lines (expected errors are listed explicitly), restart persistence and fly isolation.
- **Help coverage check**: the test extracts every `operation[0]`/`operation[1]` keyword pair handled in `CommandProcessor` (by running one command per catalog syntax and checking it is not reported as unknown) and checks the reverse: every handled family has a catalog entry.
- **JAR check**: performed by running the packaged JAR on the script with `--fly` (manual verification step recorded in tasks; tests cover the same paths in-process).

## Risks / Trade-offs

- [Script outputs include BCrypt-independent data only] → responses never print hashes, so outputs are deterministic.
