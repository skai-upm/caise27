# Proposal

## Why

Delivery 3 added generic attractions, SpeedPasses, Special Passes, pass-based access, cascade refunds and extended help on top of the E1/E2 features. Each feature was verified separately; the delivery needs evidence that everything works together through the single command system, in every execution mode and with both persistence modes, and that the packaged JAR and design model correspond to the final code.

## What Changes

- New requirement that every documented command (HELP / HELP EXTENDED) is dispatched by the command processor and every dispatched command is documented, checked automatically.
- New requirement and end-to-end tests running one combined E1 + E2 + E3 command script in interactive mode, file mode and `--fly` mode, and across a restart with normal persistence.
- Verification of `mvn package` (executable JAR running the combined script) and of the UML model against the final code (existing `UmlModelTest` + `plantuml -checkonly`).
- No change to existing command behavior.

## Capabilities

### New Capabilities
<!-- None. -->

### Modified Capabilities
- `command-processing`: added requirement "Documented and dispatched commands match".
- `application-architecture`: added requirement "Integrated Delivery 3".

## Impact

- Tests only (`AppTest`, `CommandProcessorTest`); production code changes only if the integration checks reveal a gap.
