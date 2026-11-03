# Spec Delta

## ADDED Requirements

### Requirement: Deletion constrained by tickets and groups
`USER DELETE` SHALL be rejected while the user has an active ticket (dated on or after the current system date), standalone or composing a group ticket, or belongs to a group locked by an active group ticket; when permitted, the user SHALL also be removed from all their groups, and historical tickets SHALL be retained.

#### Scenario: Active individual ticket
- **WHEN** a user with a ticket dated today requests deletion with correct credentials
- **THEN** the deletion is rejected

#### Scenario: Member of a locked group
- **WHEN** a user belongs to a group with an active group ticket and requests deletion
- **THEN** the deletion is rejected

#### Scenario: Only historical tickets
- **WHEN** a user whose tickets are all historical is deleted
- **THEN** the user is deleted, removed from their groups, and the historical tickets remain
