# Spec Delta

## ADDED Requirements

### Requirement: User deletion blocked by active tickets
`USER DELETE` SHALL be rejected while any active ticket (dated on or after the current system date) is associated with the user. Historical tickets alone SHALL NOT prevent deletion, and they SHALL keep the user information they reference after the user is deleted. This locked state SHALL be derived from the existing tickets and the current system date, not stored.

#### Scenario: Active ticket blocks deletion
- **WHEN** Ana has ticket `T001` dated `2027-04-15`, the system date is `2027-04-15` and `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** an error is printed and Ana still exists

#### Scenario: Future ticket deleted first
- **WHEN** the system date is `2027-04-01`, Ana's only ticket `T001` is dated `2027-04-15`, `TICKET DELETE;T001` and then `USER DELETE;ana@correo.com;Clave1!` are processed
- **THEN** both succeed

#### Scenario: Historical ticket does not block
- **WHEN** Ana's only ticket is dated `2027-04-15`, the system date is `2027-04-20` and `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** Ana is deleted and the ticket remains with Ana's data
