# Spec Delta

## Purpose

Keeps group tickets and their constituent individual tickets across executions without duplicating user or group data.

## ADDED Requirements

### Requirement: Group ticket file format
The system SHALL store group tickets in a text file, one per line, as `Id;IdGrupo;yyyy-MM-dd;PrecioTotal;PrecioIndividual;DNI1,DNI2,...`, listing the DNIs of the constituent tickets' users in purchase order.

#### Scenario: Line written for a group ticket
- **GIVEN** `G01` with Ana (`12345678Z`) and Pedro (`87654321X`) and price 40.00
- **WHEN** `GROUPTICKET CREATE;TG01;G01;2026-10-15` succeeds
- **THEN** the file contains `TG01;G01;2026-10-15;80.00;40.00;12345678Z,87654321X`

### Requirement: Save after group ticket changes
After every successful GROUPTICKET CREATE or DELETE the file SHALL reflect the current group tickets; rejected commands SHALL leave it unchanged.

#### Scenario: Rejected purchase leaves file intact
- **WHEN** a GROUPTICKET CREATE for a nonexistent group is rejected
- **THEN** the file content is unchanged

### Requirement: Restore group tickets at startup
On startup the system SHALL load group tickets after users and groups, linking each to its group and rebuilding one constituent ticket per stored DNI linked to the loaded user. A group or user that no longer exists SHALL be kept as a historical reference by its stored identifier. A missing file SHALL mean no group tickets.

#### Scenario: Restart keeps locks and restrictions
- **GIVEN** a previous run bought `TG01` for `G01` dated 2026-10-15
- **WHEN** the application restarts and processes `SYSTEM DATE;2026-10-01`
- **THEN** `GROUP DELETE;G01` and the deletion of any member are rejected, and `TG01` cannot be bought again
