# Spec Delta

## ADDED Requirements

### Requirement: HELP lists SpeedPass entry
`HELP` and `HELP EXTENDED` SHALL also list `ATTRACTION ENTER;<attraction>;<ticket>;SPEED`.

#### Scenario: Help shows the SPEED variant
- **WHEN** `HELP` is processed
- **THEN** `ATTRACTION ENTER;<attraction>;<ticket>;SPEED` is shown
