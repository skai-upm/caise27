# Proposal

## Why

Delivery 3 added layers, controllers, repositories, invoices, help, password hashing, passes and typed attractions in separate changes. The delivery must be verified as a whole — every E1, E2 and F3 command working together in every execution mode — and any integration defect found must be fixed.

## What Changes

- Add an end-to-end integration suite: a representative command file covering all E1, E2 and F3 commands run through the application entry point in file mode, interactive mode and `--fly` mode, asserting results and persisted state.
- Verify help coverage: every registered command is recognised by command processing and appears in `HELP` (syntax) and `HELP EXTENDED` (syntax and description).
- Verify the release: `mvn package` builds the executable JAR, which runs the integration file; the UML model and layer rules match the final code.
- Fix only integration defects discovered by this verification; no new functionality.

The source suggests change ID `complete-f3-integration`; this batch uses the BOTL ID `botl-0020-20-f3-full-integration-of-delivery-3`. Mandatory milestone message: `[F3-C10] Complete delivery 3 integration`.

## Capabilities

### New Capabilities
- `delivery-three-integration`: Release-level guarantees that all deliveries work together in every execution mode.

### Modified Capabilities
- None.

## Impact

- New integration tests and a test resource command file; production code changes only if a defect is found.
