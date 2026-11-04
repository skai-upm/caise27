# Proposal

## Why

Delivery 3 added layering, invoices, singletons and injection, help variants, password hashing, passes, typed attractions, pass-based access and cascades on top of Deliveries 1 and 2. Before closing the delivery, all features must be shown to work together in the packaged application and in every execution mode, and any integration defect fixed.

## What Changes

- Add an end-to-end command file (`src/test/resources/e2e/delivery3.txt`) with representative E1, E2 and F3 commands and its expected output, and an `IntegrationTest` running it in file mode, interactive mode and `--fly` (with permanent data present), comparing output and checking isolation and cleanup.
- Add a help-coverage check that every registered syntax appears in `HELP` and `HELP EXTENDED`.
- Produce a verification matrix (design) mapping each Delivery 3 acceptance criterion to its evidence; run the packaged JAR outside Maven.
- Fix only integration defects found by these checks; no new functionality.

## Capabilities

### New Capabilities
- `delivery-integration`: end-to-end run, temporary integrated run, help coverage, release build.

### Modified Capabilities
_None._

## Impact

- Test sources and resources only, unless a defect is found (then the minimal fix is documented in the design).
- Delivery milestone message: `[F3-C10] Complete delivery 3 integration`.
