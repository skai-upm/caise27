# Proposal

## Why

All Delivery 3 features (layers, singletons and injected repositories, invoices, extended help, BCrypt, passes, typed attractions, pass-aware entry, cascades) were built change by change. Delivery 3 must now be verified end-to-end, together with every Delivery 1 and 2 command and all execution modes, fixing only defects that the verification reveals.

## What Changes

- A representative end-to-end command file `samples/input_fase3.txt` covering E1, E2 and F3 commands.
- An integration test suite running it through the shared command processing in file mode, interactive mode and `--fly`, checking HELP/HELP EXTENDED coverage, final persistence and isolation.
- Verification of the executable JAR outside the IDE (`mvn package` + `java -jar` in a clean directory, normal and `--fly`) and of `uml/model.puml` against the final code.
- A verification matrix (design.md) mapping each acceptance criterion to its evidence.
- No new product functionality; integration defects, if any, are fixed and listed.

## Capabilities

### New Capabilities
- `delivery-integration`: end-to-end guarantees of the complete Delivery 3 application.

### Modified Capabilities
_None._

## Impact

- New `samples/input_fase3.txt` and `Delivery3IntegrationTest`; production code only if a defect is found.
