# Spec Delta

## ADDED Requirements

### Requirement: HELP lists ticket commands
The HELP output SHALL also include `TICKET CREATE;<identifier>;<email>;<date>` and `TICKET DELETE;<identifier>`.

#### Scenario: Help shows ticket commands
- **WHEN** the user enters `HELP`
- **THEN** the output contains `TICKET CREATE;<identifier>;<email>;<date>` and `TICKET DELETE;<identifier>`
