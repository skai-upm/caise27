# Proposal

## Why

Delivery 3 was built in nine changes: layers, invoices, singletons and DI, extended help, BCrypt, passes, typed attractions, pass-based entry and cascades. It must be confirmed to work end to end with every E1/E2 feature, in every execution mode, from the packaged JAR, with up-to-date help and UML. Only integration defects found by that verification may be fixed.

## What Changes

- Add a repeatable end-to-end command scenario (`src/test/resources/integration/delivery3.txt`) covering every command family from E1, E2 and F3.
- Add `Delivery3IntegrationTest`, which runs the scenario through file, interactive and `--fly` modes via `App.launch`. It checks the exact results, the equivalence of the modes, restart persistence of the final state, `--fly` isolation, and that `HELP`/`HELP EXTENDED` cover every accepted operation.
- Verify the packaged JAR outside the IDE against the same scenario, and keep the verification matrix (see design.md).
- **Integration defects:** an exploratory end-to-end run of the current JAR found none, so no production code changes are planned. Any defect found while implementing the tests will be fixed minimally and recorded.

## Capabilities

### New Capabilities
- `delivery-integration`: End-to-end scenario, mode equivalence, final-state persistence and artifact/documentation consistency.

### Modified Capabilities
_None._

## Impact

- New test resource and integration test only. Production code, commands and file formats are unchanged unless a defect is found.
