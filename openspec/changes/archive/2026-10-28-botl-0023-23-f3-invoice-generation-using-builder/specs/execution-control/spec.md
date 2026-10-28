# Spec Delta

## ADDED Requirements

### Requirement: HELP lists invoice commands
`HELP` SHALL also list `INVOICE CREATE;USER;<email>` and `INVOICE CREATE;GROUP;<groupIdentifier>`.

#### Scenario: Help shows invoices
- **WHEN** `HELP` is processed
- **THEN** both invoice command forms are shown
