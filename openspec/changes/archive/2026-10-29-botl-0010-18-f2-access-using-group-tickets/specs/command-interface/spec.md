# Spec Delta

## ADDED Requirements

### Requirement: HELP lists attraction exit and status
The HELP output SHALL include the syntaxes `ATTRACTION EXIT;<attraction>;<email>` and `ATTRACTION STATUS;<attraction>`.

#### Scenario: HELP shows ATTRACTION EXIT and STATUS
- **WHEN** the line `HELP` is processed
- **THEN** the output contains `ATTRACTION EXIT;<attraction>;<email>` and `ATTRACTION STATUS;<attraction>`
