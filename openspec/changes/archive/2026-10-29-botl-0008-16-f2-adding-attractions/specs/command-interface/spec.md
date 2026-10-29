# Spec Delta

## ADDED Requirements

### Requirement: HELP lists attraction commands
The HELP output SHALL include the syntaxes `ATTRACTION CREATE;<identifier>;<name>;<capacity>` and `ATTRACTION LIST;<attraction>`.

#### Scenario: HELP shows ATTRACTION commands
- **WHEN** the line `HELP` is processed
- **THEN** the output contains `ATTRACTION CREATE;<identifier>;<name>;<capacity>` and `ATTRACTION LIST;<attraction>`
