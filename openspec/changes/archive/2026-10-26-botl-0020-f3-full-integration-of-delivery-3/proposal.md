# Proposal

## Why

Delivery 3 features were added change by change. Before closing the delivery, the complete application must be shown to work as a whole: every E1, E2 and E3 command through the same command system, layers, controllers and repositories, in interactive, command-file and `--fly` modes, from the packaged JAR, with help and UML matching the code.

## What Changes

- Add a reference command script `examples/input_fase3.txt` exercising users, groups, dates and prices, individual and group tickets, attraction types, SpeedPass/Special Pass purchase, entry and refund, invoices, cascade deletion, help and exit.
- Add integration tests that run the script through `App` in command-file, interactive and `--fly` modes, check persistence across restarts in normal mode and isolation in `--fly`, and check that every command handled by the command processor is listed in `HELP` and `HELP EXTENDED`.
- Verify `mvn package` produces an executable `target/parque.jar` that runs the script, and that `uml/model.puml` matches the final code.
- Fix any integration gap found while doing so (none expected in command syntax).

## Capabilities

### New Capabilities
- `application-integration`: End-to-end guarantees for the complete Delivery 3 application (all commands together, all execution modes, packaged JAR, help coverage).

### Modified Capabilities
<!-- none -->

## Impact

- New `examples/input_fase3.txt` and integration test class; no production behaviour change expected.
