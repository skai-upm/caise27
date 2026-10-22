# Spec Delta

## MODIFIED Requirements

### Requirement: User deletion command
`USER DELETE;Email;Password` SHALL locate the user by email. If found and the password matches, it SHALL check that the user has no active ticket (standalone or within a group ticket) and belongs to no group locked by an active group ticket. If allowed, it SHALL remove the user from their groups and from any attraction, delete the user, persist, and print `USER DELETED`. Otherwise it SHALL print an error beginning with `ERROR` and change nothing. Historical tickets SHALL remain.

#### Scenario: Successful deletion
- **WHEN** `USER DELETE;pedro@correo.com;Otra2#` is processed and that user exists with that password
- **THEN** `USER DELETED` is printed and the user no longer exists, including after a restart

#### Scenario: Wrong password
- **WHEN** `USER DELETE;pedro@correo.com;Wrong1!` is processed
- **THEN** an error is printed and the user still exists

#### Scenario: Unknown email
- **WHEN** `USER DELETE;nobody@correo.com;Clave1!` is processed and no user has that email
- **THEN** an error is printed

#### Scenario: Active individual ticket blocks deletion
- **WHEN** the system date is `2027-04-15`, Ana holds ticket `T001` dated `2027-04-15` or later, and `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** an error is printed and Ana still exists

#### Scenario: Active group ticket blocks deletion
- **WHEN** Pedro is included in a group ticket dated on or after the system date and `USER DELETE` is processed for Pedro with the correct password
- **THEN** an error is printed and Pedro still exists

#### Scenario: Deletion allowed after future ticket removed
- **WHEN** Marta's only ticket `T002` is dated after the system date, `TICKET DELETE;T002` is processed, and then `USER DELETE;marta@correo.com;Marta4%` is processed
- **THEN** `USER DELETED` is printed

#### Scenario: Historical tickets do not block deletion
- **WHEN** the user's only tickets are dated before the system date
- **THEN** `USER DELETE` with correct credentials succeeds and the historical tickets are kept

#### Scenario: Deleted user removed from unlocked groups
- **WHEN** Laura belongs to group `G02`, which has no active group ticket, and Laura is deleted
- **THEN** `G02` no longer contains Laura
