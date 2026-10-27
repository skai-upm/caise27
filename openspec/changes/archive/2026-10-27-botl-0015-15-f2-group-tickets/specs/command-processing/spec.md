# Spec Delta

## ADDED Requirements

### Requirement: HELP lists group ticket commands
The HELP output SHALL also include `GROUPTICKET CREATE;<identifier>;<group>;<date>` and `GROUPTICKET DELETE;<identifier>`.

#### Scenario: Help shows group ticket commands
- **WHEN** the user enters `HELP`
- **THEN** the output contains `GROUPTICKET CREATE;<identifier>;<group>;<date>` and `GROUPTICKET DELETE;<identifier>`
