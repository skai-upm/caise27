## MODIFIED Requirements

### Requirement: Derived group modifiability
A group SHALL be modifiable exactly when it has no associated group ticket whose date is equal to or later than the current system date. The modifiable (locked) state SHALL NOT be stored as an independent attribute; it SHALL be determined from the group's tickets and the current system date.

#### Scenario: Group without tickets is modifiable
- **WHEN** a group has no associated group tickets
- **THEN** GROUP ADD and GROUP REMOVE are permitted on it

#### Scenario: Active group ticket locks the group
- **WHEN** group `G01` has a group ticket dated on or after the current system date and `GROUP ADD;G01;pedro@correo.com` or `GROUP REMOVE;G01;ana@correo.com` is processed
- **THEN** an error message is displayed and the membership of `G01` is unchanged

#### Scenario: Group unlocked when tickets are historical or deleted
- **WHEN** every group ticket of `G01` is dated before the current system date or has been deleted with `GROUPTICKET DELETE`
- **THEN** GROUP ADD and GROUP REMOVE are permitted on `G01` again

## ADDED Requirements

### Requirement: GROUP DELETE command
The system SHALL accept `GROUP DELETE;Identifier` and delete the group only if it has no associated group ticket dated on or after the current system date. Historical group tickets of a deleted group SHALL be retained, keeping identifier, date, price and composition, with their group reference becoming null. An unknown group or a locked group SHALL be rejected with an error message.

#### Scenario: Delete a group with only historical tickets
- **WHEN** group `G01` only has group tickets dated before the current system date and `GROUP DELETE;G01` is processed
- **THEN** group `G01` no longer exists and its historical group tickets still exist with no group reference

#### Scenario: Group with an active ticket cannot be deleted
- **WHEN** group `G01` has a group ticket dated today and `GROUP DELETE;G01` is processed
- **THEN** an error message is displayed and group `G01` still exists
