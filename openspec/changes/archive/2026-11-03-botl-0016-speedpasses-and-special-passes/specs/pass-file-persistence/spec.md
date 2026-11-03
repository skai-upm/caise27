# Spec Delta

## Purpose

Keeps passes, their prices, associated tickets and consumption status across executions.

## ADDED Requirements

### Requirement: Pass file format
The system SHALL store passes in a text file, one per line, as `Id;Tipo;IdTicket;Precio;Consumido`, where `Tipo` is `SPEED` or `SPECIAL` and `Consumido` is `true` or `false`, and SHALL save it after every successful purchase or refund.

#### Scenario: Lines written
- **WHEN** `SPEEDPASS CREATE;S1;T001;1` and `SPECIALPASS CREATE;SP1;T001` succeed for `T001` priced 40.00
- **THEN** the file contains `S1-1;SPEED;T001;8.00;false` and `SP1;SPECIAL;T001;40.00;false`

### Requirement: Restore passes at startup
On startup the system SHALL load the passes after the tickets and link each to its individual or constituent ticket, keeping price and consumed status; a missing file SHALL mean no passes and records whose ticket no longer exists SHALL be skipped.

#### Scenario: Restart
- **GIVEN** a previous run bought `S1-1` and `SP1` for `T001`
- **WHEN** the application restarts
- **THEN** both passes exist with their prices, `SPECIALPASS CREATE;SP2;T001` is rejected and `SPEEDPASS CREATE;S1;T001;1` is rejected as a duplicate identifier
