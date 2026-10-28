# Spec Delta

## ADDED Requirements

### Requirement: Deleted users leave their groups
When a user is deleted, the user SHALL also be removed from every group it belonged to, so that groups only contain existing users.

#### Scenario: Member is deleted
- **WHEN** Ana belongs to group `G01` and `USER DELETE;ana@correo.com;Clave1!` succeeds
- **THEN** `G01` no longer contains Ana
