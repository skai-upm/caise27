# Design

## Context

The application is a layered Maven project (`presentation`, `business.{controller,service,model,invoice,security}`, `persistence`) wired in `App`; `ParkController` is a per-execution Singleton; data files are siblings of `users.txt` (or of a `--fly` temporary directory); the executable JAR is shaded with jBCrypt. Each feature has unit and command-level tests, but there is no single test exercising all features together.

## Goals / Non-Goals

**Goals:** one reproducible end-to-end scenario run in all modes, its expected output under version control, and an explicit verification matrix.

**Non-Goals:** new commands or behaviour changes; rewriting passing code.

## Decisions

- **End-to-end file** `src/test/resources/e2e/delivery3.txt` and its golden output `delivery3.expected.txt`. The script covers: HELP and HELP EXTENDED; user create/update/invalid/delete-blocked; groups; SYSTEM DATE and SYSTEM PRICE; individual and group tickets with discount; SpeedPass pack and Special Pass purchase, refund; GENERAL/INDIVIDUAL/GROUP/SPECIAL attractions with normal, SPEED and Special Pass entries, a rejected type, status and exit; user and group invoices; ticket cascade deletion; a date advance that unlocks user deletion; EXIT and a command after EXIT that must not run. Outputs that contain password hashes are not printed by any command, so the golden output is deterministic.
- **`IntegrationTest`** (root test package) runs the file through `App.run`:
  1. file mode on an empty temp directory → output equals the golden file;
  2. interactive mode (file content as stdin) on another empty directory → same output;
  3. `--fly` next to pre-populated permanent files → same output, permanent files unchanged, no run directory left under `tmp/`.
- **Help coverage**: `HELP` and `HELP EXTENDED` outputs are checked against `CommandCatalog.standard()` (the existing `CommandCatalogTest` already checks registry vs dispatcher).
- **Golden output creation**: produced once by running the packaged JAR on the script, reviewed line by line against the specs, then committed; later runs compare against it.
- **Verification matrix** (Delivery 3 acceptance → evidence):

  | Criterion | Evidence |
  |---|---|
  | `mvn package` builds the executable JAR | `mvn clean package` (all tests) + `java -jar` run of the e2e file outside Maven |
  | E2 execution modes retained | `IntegrationTest` file/interactive/`--fly`; `AppTest` mode tests |
  | E1 and E2 commands available | e2e script + `CommandProcessorTest`; `LayeringTest` |
  | New commands in HELP / HELP EXTENDED | `IntegrationTest` help coverage, `CommandCatalogTest`, `HelpGeneratorTest` |
  | Normal and temporary persistence with E3 | `IntegrationTest` `--fly`, restart tests for passes/attraction types, `AppTest` fly tests |
  | UML corresponds to final code | `UmlModelTest`, PlantUML `-syntax`, label/field checker |

- **Defect handling**: any failure found is fixed minimally in production code and recorded here. Result of this change: the end-to-end script (62 commands plus one blank line, 173 output lines) ran as specified in file, interactive and `--fly` modes and from the packaged JAR outside Maven; no integration defect was found, so no production code changed.

## Risks / Trade-offs

- [Golden output must be updated when messages change] → intended: it is the regression net for the delivery.
