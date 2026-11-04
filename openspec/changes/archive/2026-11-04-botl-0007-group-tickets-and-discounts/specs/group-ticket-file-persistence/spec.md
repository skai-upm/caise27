# Spec Delta

## Purpose

Defines how group tickets are stored and restored together with their group and constituent individual tickets.

## ADDED Requirements

### Requirement: Group ticket file format
The system SHALL store group tickets in `grouptickets.txt` (next to the other data files), one per line as `Identificador;Grupo;yyyy-MM-dd;PrecioUnitario;PrecioTotal;Id1,Id2,...`, where the last field lists the constituent individual ticket identifiers, which SHALL be stored in the individual ticket file like any individual ticket.

#### Scenario: Stored line
- **WHEN** `TG01` for `G01` with three members was bought at 40.00 for 2026-10-15
- **THEN** `grouptickets.txt` contains `TG01;G01;2026-10-15;40.00;108.00;TG01-1,TG01-2,TG01-3` and `tickets.txt` contains the three constituent tickets

### Requirement: Group ticket files updated consistently
After every successful group ticket purchase or deletion, both ticket files SHALL reflect exactly the current tickets; a rejected command SHALL leave them unchanged.

#### Scenario: Deletion removes all lines
- **WHEN** a future group ticket is deleted
- **THEN** its line and the lines of its constituent tickets no longer appear in the files

### Requirement: Group tickets restored at startup
On startup the system SHALL load group tickets after users, groups and individual tickets, resolving the group identifier to the existing group and the constituent identifiers to the loaded individual tickets; locks and deletion restrictions SHALL apply to restored group tickets; a group that no longer exists SHALL be kept as its identifier for history.

#### Scenario: Restart restores lock
- **GIVEN** `G01` bought a future group ticket and the application restarted
- **WHEN** `GROUP ADD` is processed for `G01`
- **THEN** an `ERROR:` response is printed

#### Scenario: Restart restores composition
- **GIVEN** `TG01` with three constituents was saved
- **WHEN** the application restarts
- **THEN** `TG01` is composed of the same three individual tickets, which reference the registered users
