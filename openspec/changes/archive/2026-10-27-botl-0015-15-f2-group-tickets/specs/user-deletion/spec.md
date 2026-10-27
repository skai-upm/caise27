# Spec Delta

## ADDED Requirements

### Requirement: Members of locked groups cannot be deleted
USER DELETE SHALL be rejected when the user belongs to a group that has an associated group ticket dated on or after the current system date; when every group of the user is modifiable, the user is removed from them as part of the deletion.

#### Scenario: Member of a locked group
- **WHEN** Ana belongs to `G01`, which has an active group ticket, and `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** an error is reported and Ana is neither deleted nor removed from `G01`

#### Scenario: Component ticket counts as active ticket
- **WHEN** Ana's only active ticket is the component `TG01:12345678Z` of a future group ticket
- **THEN** USER DELETE for Ana is rejected
