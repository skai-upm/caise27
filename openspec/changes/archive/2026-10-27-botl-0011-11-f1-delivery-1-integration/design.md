# Design

## Context

All Delivery 1 features exist (see specs `command-processing`, `user-management`, `persistence`, `project-build`). `CommandProcessor` catches the expected `UserOperationException` and `UncheckedIOException`, but any other runtime exception (a bug, an unexpected state) would propagate out of `CommandRunner` and end the application.

## Goals / Non-Goals

**Goals:**
- Containment of unexpected errors; end-to-end evidence of the integrated feature set; verified restrictions and from-scratch build.

**Non-Goals:**
- New commands or Delivery 2 features.

## Decisions

- **Catch-all in `CommandProcessor.process`:** wrap the dispatch in `try { ... } catch (RuntimeException e)` printing `ERROR: unexpected error processing command: <message>` and returning `true`. Located in the single shared processing path so it applies to both modes.
- **`Delivery1IntegrationTest`:** runs the section 16 example via `App.run` in file mode and interactive mode with separate temp data directories and compares outputs and stored data; also runs a sequence of invalid inputs (unknown command, malformed commands, invalid values, wrong password) and checks the application keeps working.
- **Restrictions audit:** `mvn dependency:list` (no compile/runtime deps; JUnit is test scope only, not part of the delivered JAR) and a source grep; a fresh `git clone` + `mvn package` in a scratch directory.
- **Git history (flagged):** every mandatory Delivery 1 commit exists with its exact text, but `[F1-C08]` precedes `[F0-C02]`…`[F1-C07]` because of the batch processing order used at the start of this batch; history is not rewritten.
- **Mandatory commit:** `[F1-C09] Complete delivery 1 integration` is created by the batch Git step after verification and archive.

## Risks / Trade-offs

- [Catch-all could hide bugs] → The message is printed (not swallowed silently) and tests cover expected paths explicitly.
