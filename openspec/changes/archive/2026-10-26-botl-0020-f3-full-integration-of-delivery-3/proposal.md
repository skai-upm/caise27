# Proposal

## Why

Delivery 3 added layers, controllers, invoices, help, password hashing, passes and attraction types in separate changes. Before closing the delivery, all features must be shown to work together through the command system, in every execution mode, with the Delivery 1 and 2 commands still available and the documentation (help and UML) matching the final code.

## What Changes

- An end-to-end command script for Delivery 3 (`examples/input_fase3.txt`) exercising E1, E2 and E3 commands together, shipped with the project as a usable example of file mode.
- Automated integration tests that run that script through the application in file mode, interactive mode and `--fly`, checking that every command succeeds where expected, that normal persistence survives a restart with the E3 data, and that `--fly` leaves the permanent data untouched.
- Checks that every registered command appears in `HELP` and `HELP EXTENDED`, that the packaged JAR runs, and that `uml/model.puml` still matches the code.
- No new commands and no behaviour changes, unless the integration reveals a defect.

## Capabilities

### New Capabilities
- `delivery-integration`: The guarantees for the complete Delivery 3 application — executable JAR, all execution modes, availability of E1/E2/E3 commands together, help coverage, persistence in both modes, and model consistency.

### Modified Capabilities
<!-- None -->

## Impact

- New `examples/input_fase3.txt` and `Delivery3IntegrationTest`; no production code change expected.
