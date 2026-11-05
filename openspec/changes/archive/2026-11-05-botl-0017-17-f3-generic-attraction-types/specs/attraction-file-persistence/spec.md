# Spec Delta

## MODIFIED Requirements

### Requirement: Attraction records
The system SHALL store each attraction on one line of `attractions.txt` as `Id;Nombre;Capacidad;Tipo`, rewrite the file after every successful creation, and restore the attractions on startup with their type and no users present; a line with only `Id;Nombre;Capacidad` SHALL be restored as GENERAL; a missing file SHALL mean no attractions and malformed lines (including an unknown type) SHALL be skipped with a warning.

#### Scenario: File content and restoration
- **WHEN** `ATTRACTION CREATE;A01;Tornado;20` succeeds and the application restarts
- **THEN** `attractions.txt` contains `A01;Tornado;20;GENERAL` and attraction `A01` exists again, empty, with capacity 20 and type GENERAL

#### Scenario: Records written before types existed
- **GIVEN** `attractions.txt` contains `A01;Tornado;20`
- **WHEN** the application starts
- **THEN** `A01` is a GENERAL attraction with capacity 20
