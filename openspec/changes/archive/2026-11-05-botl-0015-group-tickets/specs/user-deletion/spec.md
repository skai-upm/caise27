## ADDED Requirements

### Requirement: Group tickets block user deletion
`USER DELETE` SHALL be rejected with an error message starting with `ERROR` when the user has an active individual ticket that belongs to a group ticket composition, or when the user belongs to a group that is locked by an active group ticket, since deleting the user would modify that group. If all the user's groups are modifiable, the user is removed from them as part of the deletion.

#### Scenario: Active composition ticket blocks deletion
- **GIVEN** Ana is in the composition of active group ticket `TG01`
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** an error message starting with `ERROR` is printed and Ana is still registered

#### Scenario: Member of a locked group not deleted
- **GIVEN** Ana belongs to group `G01`, which is locked by the active group ticket `TG01`
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** an error message starting with `ERROR` is printed and Ana remains a member of `G01`
