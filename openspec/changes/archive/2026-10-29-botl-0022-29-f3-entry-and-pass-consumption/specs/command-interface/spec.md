# Spec Delta

## ADDED Requirements

### Requirement: HELP lists SpeedPass entry
The HELP and HELP EXTENDED outputs SHALL include `ATTRACTION ENTER;<attraction>;<ticket>;SPEED`.

#### Scenario: HELP shows the SPEED variant
- **WHEN** the line `HELP` is processed
- **THEN** the output contains `ATTRACTION ENTER;<attraction>;<ticket>;SPEED`
