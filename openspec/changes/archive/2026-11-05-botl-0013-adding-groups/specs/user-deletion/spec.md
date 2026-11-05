## ADDED Requirements

### Requirement: Deleted users leave their groups
When a user is deleted, the system SHALL also remove that user from every group it belongs to, so that groups only contain registered users.

#### Scenario: Member deleted
- **GIVEN** Ana belongs to group `G01`
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` succeeds
- **THEN** `G01` no longer contains Ana
