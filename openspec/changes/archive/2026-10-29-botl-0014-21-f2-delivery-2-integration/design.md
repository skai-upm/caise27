# Design

## Context

All Delivery 2 commands are implemented in `CommandProcessor`/`ParkController`; persistence and `--fly` are in `App`/`ParkStorage`. HELP prints `COMMAND_SYNTAX`.

## Goals / Non-Goals

**Goals:** Delivery 2 HELP text; proof that all features cooperate. **Non-Goals:** USER commands (later proposals).

## Decisions

- **`COMMAND_SYNTAX`** becomes exactly the §15 list in source order. Existing per-feature HELP tests are updated to the new spellings (only USER placeholders and `<attractionIdentifier>` change).
- **`Delivery2IntegrationTest`**: (a) processor-level scenario with programmatically registered users covering every Delivery 2 command, including the source examples (6-member group ticket at 40 € → 192 €; locks; deletions; entry/exit/status); (b) `App.run` in normal mode twice to confirm reconstruction after restart; (c) `App.run` with `--fly` and a command file, asserting no data remains and permanent data is untouched.
- **Packaged JAR**: run `java -jar target/amusement-park.jar input_fase2.txt` and with `--fly` from a scratch directory.

## Risks / Trade-offs

- [Users only programmatic] → stated in proposal; the JAR runs show controlled errors for user-dependent commands.
