# Spec Delta

## ADDED Requirements

### Requirement: Deleted user leaves all groups
When a user is deleted with `USER DELETE`, the system SHALL also remove that user from every group they belonged to, so that groups only contain existing users.

#### Scenario: Group membership removed on deletion
- **WHEN** `ana@correo.com` belongs to group `G01` and `USER DELETE;ana@correo.com;Clave1!` succeeds
- **THEN** group `G01` no longer contains her
