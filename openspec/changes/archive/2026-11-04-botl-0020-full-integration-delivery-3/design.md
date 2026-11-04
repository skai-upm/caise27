# Design

## Context

All features are implemented and unit/integration-tested per change; `App.run(args, stdin, out, err, baseDir)` gives an in-JVM entry point for whole executions; `ApplicationContext` wires the final configuration; `UmlModelTest` and `LayeringTest` already verify structure.

## Goals / Non-Goals

**Goals:** one representative script and one test class that prove the whole delivery end to end; a manual release check of the shaded JAR.

**Non-Goals:** new features; refactoring passing code.

## Decisions

- **Script** `samples/input_fase3.txt` (kept in the repository so it can also be run by hand: `java -jar target/parque-1.0-SNAPSHOT.jar samples/input_fase3.txt`). It contains successful and intentionally rejected commands; dates are set with `SYSTEM DATE` so the results do not depend on the real clock.
- **Expected output**: the test asserts the full list of responses (status lines; multi-line HELP/STATUS/INVOICE blocks checked by their first line and key lines) so any regression in any command is detected.
- **Modes**: file vs standard input vs `--fly` compared for identical output; `--fly` also checked for permanent files byte-identical and no `tmp/`.
- **Restart**: a second execution with follow-up commands verifies restored state (password hash, consumed pass, group lock, attraction type, invoice totals).
- **Help coverage**: every operation of `CommandCatalog.standard()` is recognised (not "Unknown command") and the set of operations in the catalog equals the set of `case` labels of `CommandProcessor` (read from source) so no command can be missing from HELP.
- **Defect policy**: only failures found by these checks are fixed, each with a regression test.
