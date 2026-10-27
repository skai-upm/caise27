# Spec Delta

## ADDED Requirements

### Requirement: HELP lists attraction exit and status
The HELP output SHALL also include `ATTRACTION EXIT;<attraction>;<email>` and `ATTRACTION STATUS;<attractionIdentifier>`.

#### Scenario: Help shows exit and status
- **WHEN** the user enters `HELP`
- **THEN** the output contains `ATTRACTION EXIT;<attraction>;<email>` and `ATTRACTION STATUS;<attractionIdentifier>`
