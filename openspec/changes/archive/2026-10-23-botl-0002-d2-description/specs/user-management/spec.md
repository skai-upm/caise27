# Spec Delta

## MODIFIED Requirements

### Requirement: User deletion
`USER DELETE;Email;Password` SHALL locate the user by email, verify it exists and that the password matches exactly, and then delete the user only if they have no active ticket (an individual ticket or a member ticket of a group ticket dated on or after the system date) and belong to no locked group. On success the user is removed from all their groups, persistent storage is updated, and historical tickets keep the user's information. These restrictions are derived from tickets and the system date, not stored. Otherwise all data is left unchanged.

#### Scenario: Successful deletion
- **WHEN** `USER DELETE;pedro@correo.com;Otra2#` is processed and that user exists with that password
- **THEN** `USER DELETED` is printed and the user no longer exists, including after a restart

#### Scenario: Wrong password
- **WHEN** `USER DELETE;ana@correo.com;Wrong1!` is processed and Ana's password is `Clave1!`
- **THEN** an authentication error is printed and the user is not deleted

#### Scenario: Unknown user
- **WHEN** `USER DELETE;nobody@correo.com;Clave1!` is processed and no such user exists
- **THEN** an error that the user was not found is printed

#### Scenario: Active individual ticket
- **WHEN** Marta has a ticket dated on or after the system date and `USER DELETE;marta@correo.com;Marta4%` is processed
- **THEN** an error is printed and Marta is not deleted

#### Scenario: Deletion allowed after deleting the future ticket
- **WHEN** Marta's only ticket T002 is in the future, `TICKET DELETE;T002` succeeds and then `USER DELETE;marta@correo.com;Marta4%` is processed
- **THEN** `USER DELETED` is printed

#### Scenario: Member of an active group ticket
- **WHEN** Ana's group G01 has a group ticket for today and Ana is deleted
- **THEN** an error is printed and Ana is not deleted

#### Scenario: Removed from unlocked groups
- **WHEN** Pedro belongs to unlocked group G02, has only historical tickets, and is deleted
- **THEN** `USER DELETED` is printed, Pedro is no longer a member of G02, and his historical tickets still exist
