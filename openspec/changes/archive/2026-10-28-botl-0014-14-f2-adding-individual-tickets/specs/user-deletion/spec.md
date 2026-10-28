# Spec Delta

## ADDED Requirements

### Requirement: Active tickets block user deletion
`USER DELETE` SHALL be rejected with an `ERROR:` message while any ticket associated with the user (an individual ticket, including those comprising a group ticket) is dated on or after the current system date. Historical tickets alone SHALL NOT prevent deletion. The user's locked state SHALL be derived from the existing tickets and the current system date, never stored.

#### Scenario: User with a ticket for today
- **WHEN** the current system date is 2027-04-15, Ana has ticket `T001` dated 2027-04-15 and `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** an `ERROR:` line about active tickets is printed and Ana still exists

#### Scenario: User with only historical tickets
- **WHEN** Ana's only ticket is dated before the current system date and `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** `USER DELETED` is printed and the historical ticket is kept with its user information

#### Scenario: Future ticket deleted first
- **WHEN** Ana's only ticket is dated after the current system date, `TICKET DELETE` removes it, and then `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** `USER DELETED` is printed

### Requirement: Locked groups block user deletion
`USER DELETE` SHALL be rejected with an `ERROR:` message if the user belongs to a group that is locked by an active group ticket, because removing the user would modify that group's composition.

#### Scenario: Member of a locked group
- **WHEN** Ana belongs to a group with a group ticket dated on or after the current system date and `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** an `ERROR:` line about the locked group is printed and Ana still exists and remains in the group
