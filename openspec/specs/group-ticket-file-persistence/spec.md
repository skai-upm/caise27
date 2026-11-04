# group-ticket-file-persistence Specification

## Purpose

Defines how group tickets and their constituent individual tickets are stored in a text file, restored on startup, and retained as history.

## Requirements

### Requirement: Group ticket records
The system SHALL store each group ticket on one line of `grouptickets.txt` as `Id;yyyy-MM-dd;PrecioTotal;IdGrupo;PrecioIndividual;DNI1;DNI2;...` (one DNI per constituent ticket, in order), rewriting the file after every successful group ticket creation or deletion; the individual tickets file format SHALL remain unchanged.

#### Scenario: File content
- **WHEN** `GROUPTICKET CREATE;TG01;G01;2026-10-15` succeeds for Ana and Pedro at `40.00`
- **THEN** `grouptickets.txt` contains `TG01;2026-10-15;80.00;G01;40.00;12345678Z;87654321X`

### Requirement: Group ticket restoration and history
On startup the system SHALL restore each group ticket linked to its group and to the users of its constituent tickets, rebuilding constituent identifiers as `<Id>-<DNI>`; a line whose group or any constituent user no longer exists SHALL be kept unchanged as a historical record, with its identifiers still reserved.

#### Scenario: Restart restores composition and lock
- **GIVEN** `TG01` for `G01` dated `2026-10-15` was stored
- **WHEN** the application restarts with system date `2026-10-01`
- **THEN** `TG01` is restored with its constituent tickets and `G01` is locked
