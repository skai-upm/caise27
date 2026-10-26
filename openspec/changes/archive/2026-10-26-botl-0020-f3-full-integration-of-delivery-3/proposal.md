# Proposal

## Why

Delivery 3 added layers, singletons, builders, passes and typed attractions across nine changes. Before closing the delivery we need evidence that every E1, E2 and E3 command works together in all execution modes, that the packaged JAR runs, and that help and the UML match the final code — plus a short guide for running the application.

## What Changes

- An end-to-end command script (`src/test/resources/integration/delivery3.txt`) exercising E1 (users), E2 (date, groups, tickets, group tickets, attractions, prices) and E3 (invoices, passes, typed attractions, SPEED/SPECIAL entry, extended help) commands.
- `DeliveryIntegrationTest` runs that script through `App` in file mode, interactive mode and `--fly`, checks the expected responses, that normal mode persists every E3 file in `data/` and `--fly` leaves nothing behind, and that HELP / HELP EXTENDED list every registered command.
- Verification that `mvn package` produces `target/parque.jar` that runs the script with `java -jar`.
- `README.md` documents build, execution modes and the command list (pointing to `HELP EXTENDED`).
- Final check that `uml/model.puml` matches the code.
- No functional change. Required commit title: `[F3-C10] Complete delivery 3 integration`.

## Capabilities

### New Capabilities

### Modified Capabilities
- `project-build`: an automated end-to-end integration check of all deliveries and run documentation are part of the build.

## Impact

- New test resource and test class; README rewritten; no production code changes expected.
