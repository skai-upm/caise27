# Spec Delta

## ADDED Requirements

### Requirement: HELP lists attraction creation and listing
The HELP output SHALL also include `ATTRACTION CREATE;<identifier>;<name>;<capacity>` and `ATTRACTION LIST;<attraction>`.

#### Scenario: Help shows attraction commands
- **WHEN** the user enters `HELP`
- **THEN** the output contains `ATTRACTION CREATE;<identifier>;<name>;<capacity>` and `ATTRACTION LIST;<attraction>`
