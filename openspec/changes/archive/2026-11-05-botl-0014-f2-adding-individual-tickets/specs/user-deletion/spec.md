# Spec Delta

## ADDED Requirements

### Requirement: Active tickets prevent user deletion
`USER DELETE` MUST be rejected with an `ERROR` response, leaving the user unchanged, while any active ticket (dated on or after the current system date) is associated with the user, including individual tickets that form part of a group ticket. Historical tickets alone SHALL NOT prevent deletion. This state SHALL be derived from the tickets managed by the system and the current system date, not stored in the user.

#### Scenario: Future ticket blocks deletion
- **WHEN** Ana has ticket `T001` dated after the system date and `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** an `ERROR` response is printed and Ana still exists

#### Scenario: Deletion allowed after deleting the future ticket
- **WHEN** `TICKET DELETE;T001` succeeds and then `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** the user is deleted

#### Scenario: Historical ticket does not block deletion
- **WHEN** Ana's only ticket is dated before the current system date
- **THEN** `USER DELETE;ana@correo.com;Clave1!` succeeds and the historical ticket is retained with its user information

### Requirement: Locked groups prevent user deletion
`USER DELETE` MUST be rejected with an `ERROR` response while the user belongs to a group that has an active group ticket, because removing the user would modify that group's composition.

#### Scenario: Member of unlocked groups deleted
- **WHEN** Ana belongs only to groups without active group tickets and has no active tickets
- **THEN** `USER DELETE` succeeds and she is removed from those groups
