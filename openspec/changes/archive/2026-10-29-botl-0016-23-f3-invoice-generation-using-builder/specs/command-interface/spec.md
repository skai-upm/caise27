# Spec Delta

## ADDED Requirements

### Requirement: HELP lists invoice commands
The HELP output SHALL include the syntaxes `INVOICE CREATE;USER;<email>` and `INVOICE CREATE;GROUP;<groupIdentifier>`.

#### Scenario: HELP shows INVOICE commands
- **WHEN** the line `HELP` is processed
- **THEN** the output contains `INVOICE CREATE;USER;<email>` and `INVOICE CREATE;GROUP;<groupIdentifier>`
