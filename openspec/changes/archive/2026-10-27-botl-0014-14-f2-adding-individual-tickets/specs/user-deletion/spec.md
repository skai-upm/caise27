# Spec Delta

## ADDED Requirements

### Requirement: Active tickets block user deletion
USER DELETE SHALL be rejected while any ticket dated on or after the current system date is associated with the user; tickets dated before the current date SHALL NOT prevent deletion. This state SHALL be derived from the existing tickets and the system date, not stored on the user.

#### Scenario: User with a future ticket
- **WHEN** the system date is 2027-04-10, Ana has ticket `T001` for 2027-04-15 and `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** an error about active tickets is reported and Ana is not deleted

#### Scenario: Future ticket deleted first
- **WHEN** Ana's only active ticket `T001` is deleted with `TICKET DELETE;T001` and then `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** the response is `USER DELETED`

#### Scenario: Only historical tickets
- **WHEN** Ana's only ticket is dated 2027-04-15 and the system date has been moved to 2027-04-16
- **THEN** `USER DELETE;ana@correo.com;Clave1!` responds `USER DELETED` and the historical ticket is still kept
