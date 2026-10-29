# Spec Delta

## ADDED Requirements

### Requirement: HELP lists attraction entry
The HELP output SHALL include the syntax `ATTRACTION ENTER;<attraction>;<ticket>`.

#### Scenario: HELP shows ATTRACTION ENTER
- **WHEN** the line `HELP` is processed
- **THEN** the output contains `ATTRACTION ENTER;<attraction>;<ticket>`
