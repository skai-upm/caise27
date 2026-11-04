# Spec Delta

## Purpose

Defines how group tickets and their constituent individual tickets are stored in a text file and restored, linked to the existing group and users, when the application starts again.

## ADDED Requirements

### Requirement: Group ticket file format
Group tickets SHALL be stored in a UTF-8 text file separate from the other data files, one group ticket per line, as `Id;yyyy-MM-dd;IdGrupo;PrecioIndividual;PrecioTotal` followed by one `;`-separated field per constituent ticket containing its member's DNI. Prices SHALL be formatted with two decimals. The individual tickets file SHALL NOT contain constituent tickets.

#### Scenario: File content
- **GIVEN** the price is 40.00 and `G01` has members with DNIs `12345678Z`, `87654321X` and `11111111H`
- **WHEN** `GROUPTICKET CREATE;TG01;G01;2026-10-15` succeeds
- **THEN** the group tickets file contains the line `TG01;2026-10-15;G01;40.00;108.00;12345678Z;87654321X;11111111H`

### Requirement: Group ticket file reflects every successful change
After each successful `GROUPTICKET CREATE` or `GROUPTICKET DELETE` the group tickets file SHALL contain exactly the current group tickets. A rejected command SHALL NOT change it.

#### Scenario: Rejected group ticket command
- **WHEN** a `GROUPTICKET` command is rejected
- **THEN** the group tickets file content is unchanged

### Requirement: Group ticket restart recovery
On startup the application SHALL load the group tickets after users and groups, link each to the existing group with the stored identifier and rebuild its constituent tickets linked to the existing users with the stored DNIs, without creating new user or group objects. A group or DNI that no longer exists SHALL be kept as historical reference without an object link. A missing file SHALL mean no group tickets.

#### Scenario: Restore after restart
- **GIVEN** `TG01` was bought for `G01` with three members in a previous run
- **WHEN** the application restarts with the same files
- **THEN** `TG01` has the same date and total price, refers to the same `G01` object returned by group lookup, its three constituent tickets refer to the existing users, and `G01` is still locked while `TG01` is current or future
