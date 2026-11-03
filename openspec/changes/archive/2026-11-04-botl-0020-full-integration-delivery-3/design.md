# Design

## Context

The application is complete after F3-C01…C09: `App` is the composition root (normal or `--fly` data directory), `CommandProcessor` dispatches 28 commands to `ParkController` and the singleton controllers, file repositories implement repository interfaces, and the help is generated from `CommandCatalog`. Existing tests cover each feature separately. See proposal.md.

## Goals / Non-Goals

**Goals:** prove that everything works together and in all modes; produce traceable evidence.

**Non-Goals:** new behaviour, refactoring of passing code.

## Decisions

- **End-to-end file** `samples/input_fase3.txt` (kept in the repository so it can be run outside the IDE): E1 users and validation errors, E2 date/price/groups/tickets/group tickets/attractions/status, F3 typed attractions, passes, SPEED and Special Pass entries, invoices, cascades, HELP, HELP EXTENDED, EXIT and a command after EXIT.
- **`Delivery3IntegrationTest`** runs the file through `App.execute` in file mode on an empty data directory and asserts the key responses line by line; runs the same lines through standard input and compares; restarts and checks the persisted state; runs with `--fly` and checks equality and isolation; checks that every catalogue command is dispatched.
- **Outside the IDE:** `mvn clean package`, then `java -jar` of the shaded JAR in a fresh directory with the sample file (normal and `--fly`).

## Verification matrix

| Acceptance criterion (F3-C10) | Evidence |
|---|---|
| `mvn package` generates the executable JAR | `mvn clean package` (tests included) + `java -jar` from a clean directory |
| E2 execution modes retained | `Delivery3IntegrationTest`: file, standard input and `--fly` runs of the same file |
| E1 and E2 commands available | sample file sections E1/E2; existing per-feature tests |
| New commands in HELP and HELP EXTENDED | integration test: 28 entries in both, descriptions in extended, all dispatched |
| Normal and temporary persistence with E3 | restart assertions (hash, types, consumed passes) and `--fly` isolation |
| UML corresponds to final code | field-by-field script (every model-typed field → one line) + `plantuml -checkonly` |

## Risks / Trade-offs

- [Real system date in the restart check] → the test sets `SYSTEM DATE` after restart, as a user would.

## Verification results (apply phase)

| Check | Result |
|---|---|
| `mvn clean package` (with tests) | BUILD SUCCESS, 340 tests, 0 failures; shaded JAR replaces the main artifact (only `META-INF/MANIFEST.MF` overlaps, standard and handled by the manifest transformer) |
| `java -jar` in a clean directory, `samples/input_fase3.txt` | exit 0, 150 output lines, 6 data files written |
| `java -jar --fly samples/input_fase3.txt` in another clean directory | exit 0, output byte-identical to the normal run, no file or `tmp/` left |
| `Delivery3IntegrationTest` | file mode responses as expected, standard input identical, restart state (hash, groups, cascade of T002/S6-1, types, consumed passes), `--fly` isolation, 28 HELP / 56 HELP EXTENDED lines, all commands dispatched |
| `uml/model.puml` | PlantUML `-checkonly` OK; 57 production types present; 51/51 model-typed fields mapped to one labelled line |
| Integration defects found | none — no production code changed |
