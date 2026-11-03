# Spec Delta

## ADDED Requirements

### Requirement: Group locked by valid group tickets
While a group has any group ticket dated on or after the system date, `GROUP ADD`, `GROUP REMOVE` and `GROUP DELETE` on that group SHALL be rejected. When all its group tickets are dated before the system date, the group SHALL be modifiable again.

#### Scenario: Locked group
- **GIVEN** system date 2026-10-01 and group ticket `TG01` of `G01` dated 2026-10-15
- **WHEN** `GROUP ADD;G01;luis@correo.com`, `GROUP REMOVE;G01;ana@correo.com` or `GROUP DELETE;G01` is processed
- **THEN** each is rejected and the group is unchanged

#### Scenario: Lock on the ticket date
- **GIVEN** the same ticket and system date 2026-10-15
- **WHEN** `GROUP REMOVE;G01;ana@correo.com` is processed
- **THEN** it is rejected

#### Scenario: Unlocked when tickets are past
- **GIVEN** the same ticket and `SYSTEM DATE;2026-10-16`
- **WHEN** `GROUP ADD;G01;luis@correo.com` is processed
- **THEN** Luis is added
