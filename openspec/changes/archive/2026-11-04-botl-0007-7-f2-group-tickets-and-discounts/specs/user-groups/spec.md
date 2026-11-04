# Spec Delta

## ADDED Requirements

### Requirement: Groups locked by active group tickets
While a group has any group ticket dated on or after the system date, `GROUP ADD`, `GROUP REMOVE` and `GROUP DELETE` for that group SHALL be rejected without changes; once all its group tickets are dated before the system date, these commands SHALL be accepted again under their normal rules.

#### Scenario: Locked group
- **GIVEN** system date `2026-10-01` and `TG01` for `G01` dated `2026-10-15`
- **WHEN** `GROUP ADD;G01;luis@correo.com`, `GROUP REMOVE;G01;ana@correo.com` or `GROUP DELETE;G01` is processed, also with system date `2026-10-15`
- **THEN** each is rejected and `G01` is unchanged

#### Scenario: Lock released as the date advances
- **GIVEN** `G01`'s only group ticket is dated `2026-10-15`
- **WHEN** `SYSTEM DATE;2026-10-16` is processed and then `GROUP ADD;G01;luis@correo.com`
- **THEN** Luis is added to `G01`
