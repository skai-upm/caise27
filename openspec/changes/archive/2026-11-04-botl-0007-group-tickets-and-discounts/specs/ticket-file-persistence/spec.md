# Spec Delta

## ADDED Requirements

### Requirement: Group ticket file format
The system SHALL store group tickets in a separate text file, one group ticket per line, as `id;groupId;yyyy-MM-dd;individualPrice;total;dni1;dni2;...`, where the DNIs list the constituent tickets' owners in order and constituent ticket `k` (1-based) has identifier `<id>-<k>`.

#### Scenario: File contents
- **WHEN** `TG01` for `G01` dated 2026-10-15 is bought at 40.00 by members `12345678Z` and `87654321X`
- **THEN** the group tickets file contains `TG01;G01;2026-10-15;40.00;80.00;12345678Z;87654321X`

### Requirement: Restore group tickets at startup
At startup the system SHALL load group tickets after users and groups, linking each to the loaded group with that identifier and each constituent ticket to the loaded user with that DNI. Group tickets of deleted groups and constituents of deleted users SHALL be kept as history with the stored identifiers; malformed lines SHALL be ignored and a missing file SHALL mean no group tickets.

#### Scenario: Restart keeps locks and protections
- **GIVEN** a previous run bought `TG01` for `G01` dated 2026-10-15
- **WHEN** the application restarts and `SYSTEM DATE;2026-10-01` is processed
- **THEN** `GROUP DELETE;G01` and deletion of any covered member are rejected, and `TG01` has its stored total
