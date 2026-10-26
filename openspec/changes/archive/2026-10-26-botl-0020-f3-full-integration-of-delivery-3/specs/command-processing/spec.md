# Spec Delta

## ADDED Requirements

### Requirement: Documented and dispatched commands match
Every command listed by `HELP` and `HELP EXTENDED`, including the Delivery 3 commands `SPEEDPASS CREATE`, `SPEEDPASS DELETE`, `SPECIALPASS CREATE`, `SPECIALPASS DELETE`, the typed `ATTRACTION CREATE` and the `ATTRACTION ENTER` variants, SHALL be recognised by the command processor, and the processor SHALL not accept any command absent from the help.

#### Scenario: Every help entry is recognised
- **WHEN** each command of the help listing is processed with its documented number of parameters
- **THEN** none of them answers `ERROR UNKNOWN COMMAND`

#### Scenario: Delivery 3 commands in both help modes
- **WHEN** `HELP` and `HELP EXTENDED` are processed
- **THEN** both list the SpeedPass, Special Pass and typed attraction commands
