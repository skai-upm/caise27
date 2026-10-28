# Design

## Context

After F0-C01 and F1-C08 the project has `App.main` running `CommandProcessor` on standard input, with `HELP` and `EXIT`. `mvn package` produces a JAR without a `Main-Class` entry. No file-mode support exists. The test helper in `CommandProcessorTest` uses `Pattern.quote` (a regular expression), which conflicts with the Delivery 1 restrictions.

## Goals / Non-Goals

**Goals:**
- Executable JAR, interactive mode, file mode, controlled error handling, integration tests against the real JAR.

**Non-Goals:**
- USER commands, user model, persistence (milestones F1-C01…F1-C06).
- Renaming the produced JAR (placeholder name in source).

## Decisions

- **Manifest via `maven-jar-plugin` `archive.manifest.mainClass`** = `es.upm.etsisi.amusementpark.App`. Alternative: shade/assembly plugin — unnecessary since there are no runtime dependencies.
- **Mode selection in `App.main`**: `args.length == 0` → `System.in`; otherwise `Files.newBufferedReader(Path.of(args[0]))`. Both feed the same `CommandProcessor.run`, so EXIT semantics are identical.
- **Error handling**: `App` catches `IOException`/`InvalidPathException` when opening or reading the file and prints `ERROR: Cannot read command file: <path>`. `CommandProcessor.process` wraps `Command.execute` in a `try/catch (RuntimeException)` printing `ERROR: <message>` and continuing.
- **Integration tests** (`ApplicationIntegrationTest`): call `App.main` with redirected `System.in`/`System.out` for both modes; a Failsafe-free approach is used — a test launches `java -jar` on the packaged JAR only when it exists (it is verified manually/in CI via `mvn package` followed by the documented commands), keeping `mvn package` self-contained.
- **Remove regex use in tests** by replacing `Pattern.quote`/`split` counting with an `indexOf` loop.

## Risks / Trade-offs

- [Implementing JAR/file mode before F0-C02/F1-C07] → Required by this milestone's statement; later changes will verify rather than duplicate.
- [Process-level JAR test depends on the build order] → JAR execution is verified explicitly as a task with `java -jar` after `mvn package`.
