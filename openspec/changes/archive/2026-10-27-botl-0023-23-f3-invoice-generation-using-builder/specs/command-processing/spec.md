# Spec Delta

## ADDED Requirements

### Requirement: HELP lists invoice commands
The HELP output SHALL also include `INVOICE CREATE;USER;<email>` and `INVOICE CREATE;GROUP;<groupIdentifier>`.

#### Scenario: Help shows invoice commands
- **WHEN** the user enters `HELP`
- **THEN** the output contains `INVOICE CREATE;USER;<email>` and `INVOICE CREATE;GROUP;<groupIdentifier>`
