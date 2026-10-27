# Spec Delta

## ADDED Requirements

### Requirement: User deletion blocked by locked groups
`USER DELETE` SHALL be rejected when the user has an active individual ticket that belongs to a group ticket, or belongs to a group that has an active group ticket, because deleting the user would modify a locked group.

#### Scenario: Member of a locked group
- **WHEN** Ana belongs to `G01`, `G01` has group ticket `TG01` dated `2027-04-15`, the system date is `2027-04-15` and `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** an error is printed and Ana still exists and still belongs to `G01`

#### Scenario: Only historical group tickets
- **WHEN** Ana belongs to `G01`, whose only group ticket is dated `2027-04-15`, the system date is `2027-04-16` and `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** Ana is deleted, removed from `G01`, and `TG01` keeps Ana's composed ticket
