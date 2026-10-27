# Spec Delta

## ADDED Requirements

### Requirement: Help lists invoice commands
HELP SHALL also list `INVOICE CREATE;USER;<email>` and `INVOICE CREATE;GROUP;<groupIdentifier>`.

#### Scenario: Invoice syntax in help
- **WHEN** `HELP` is processed
- **THEN** both invoice syntax lines are shown
