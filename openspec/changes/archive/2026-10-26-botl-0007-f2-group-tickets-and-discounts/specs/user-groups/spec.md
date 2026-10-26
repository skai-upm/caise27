# Spec Delta

## ADDED Requirements

### Requirement: Groups locked by valid group tickets
While a group has any group ticket dated on or after the system date, the system SHALL reject `GROUP ADD`, `GROUP REMOVE` and `GROUP DELETE` for that group. Once all its group tickets are in the past, the group SHALL be modifiable again.

#### Scenario: Locked group
- **WHEN** `G01` has group ticket `TG01` for 2026-10-15, the system date is 2026-10-15 and `GROUP ADD;G01;luis@correo.com` is processed
- **THEN** an error is reported and the group is unchanged

#### Scenario: Lock released
- **WHEN** the system date moves to 2026-10-16
- **THEN** `GROUP ADD`, `GROUP REMOVE` and `GROUP DELETE` on `G01` are accepted again
