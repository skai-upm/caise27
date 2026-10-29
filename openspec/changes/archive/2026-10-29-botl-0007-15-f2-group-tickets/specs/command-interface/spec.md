# Spec Delta

## ADDED Requirements

### Requirement: HELP lists group ticket commands
The HELP output SHALL include the syntaxes `GROUPTICKET CREATE;<identifier>;<group>;<date>` and `GROUPTICKET DELETE;<identifier>`.

#### Scenario: HELP shows GROUPTICKET commands
- **WHEN** the line `HELP` is processed
- **THEN** the output contains `GROUPTICKET CREATE;<identifier>;<group>;<date>` and `GROUPTICKET DELETE;<identifier>`
