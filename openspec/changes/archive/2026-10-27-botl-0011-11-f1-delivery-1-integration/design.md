# Design

## Context

All Delivery 1 operations exist and are covered by unit tests; no test exercises the full reference flow or scans for forbidden constructs.

## Goals / Non-Goals

**Goals:** robustness net, end-to-end and restriction tests, user documentation.
**Non-Goals:** new behaviour.

## Decisions

- **Catch-all in `CommandProcessor.process`** for `RuntimeException` (after the specific `PersistenceException` handler), returning `ERROR: Command could not be processed`. This is the one place both input modes pass through.
- **`IntegrationTest`** drives `Main.run` with the §16 sequence through stdin and a temp `input.txt`, comparing outputs and the resulting users file.
- **`TechnicalRestrictionsTest`** reads `src/main/java/**/*.java` and asserts: imports start with `java.` or `es.upm.amusementpark`; no `java.util.regex`, `.matches(`, `replaceAll(`, `replaceFirst(`; `Main`, `User` and `UserFileStore` do not contain each other's responsibilities (e.g. `User` has no `java.io`/`java.nio` import; `Main` has no `Files.write`/`Files.readAllLines`).
- **README.md** with build/run commands.
