# Proposal

## Why

Delivery 3 added layers, a singleton controller with injection, invoices, extended help, password hashing, passes, typed attractions and pass-aware access on top of Deliveries 1 and 2. Before closing the delivery, all features must be verified working together through the final command system, controllers and repositories, in every execution mode, with the packaged JAR.

## What Changes

- Add an end-to-end command script `samples/input_fase3.txt` exercising E1 (users, validation, HELP, EXIT), E2 (system date and price, groups, individual and group tickets with locks, attractions and status, `--fly`) and F3 (invoices, HELP EXTENDED, hashed passwords, SpeedPasses/Special Passes, typed attractions, SPEED and SPECIAL access, cascades) commands.
- Add `Delivery3IntegrationTest` checking: every response of the script in file mode; identical output through standard input; the restored state after a restart; `--fly` producing the same output while leaving permanent files untouched and no temporary directory; `HELP` / `HELP EXTENDED` listing every registered command and every command family handled by the processor.
- Verify the release outside the test suite: `mvn clean package`, `java -jar` of the shaded JAR with the script (normal and `--fly`), interactive input, and the UML check.
- Fix only integration defects found by this verification (none are presumed).

## Capabilities

### New Capabilities
- `delivery-integration`: end-to-end guarantees of the final delivery (packaging, execution modes, command availability, help coverage, persistence, UML correspondence).

### Modified Capabilities
- None.

## Impact

- **Files**: `samples/input_fase3.txt`, `src/test/java/com/parque/Delivery3IntegrationTest.java`; production code only if a defect is found.
- **Mandatory milestone message**: `[F3-C10] Complete delivery 3 integration`.
