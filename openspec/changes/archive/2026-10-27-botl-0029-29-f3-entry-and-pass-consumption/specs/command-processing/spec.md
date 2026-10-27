# Spec Delta

## ADDED Requirements

### Requirement: HELP lists SpeedPass entry
HELP and HELP EXTENDED SHALL also include `ATTRACTION ENTER;<attraction>;<ticket>;SPEED`.

#### Scenario: Help shows SpeedPass entry
- **WHEN** the user enters `HELP`
- **THEN** the output contains `ATTRACTION ENTER;<attraction>;<ticket>;SPEED`
