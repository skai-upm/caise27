# Spec Delta

## ADDED Requirements

### Requirement: HELP lists attraction entry
The HELP output SHALL also include `ATTRACTION ENTER;<attraction>;<ticket>`.

#### Scenario: Help shows attraction entry
- **WHEN** the user enters `HELP`
- **THEN** the output contains `ATTRACTION ENTER;<attraction>;<ticket>`
