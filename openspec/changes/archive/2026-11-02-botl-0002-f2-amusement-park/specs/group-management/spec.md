# Spec Delta

## Purpose

Organizes registered users into uniquely identified groups whose membership can change only while no active group ticket locks them.

## ADDED Requirements

### Requirement: Group creation
`GROUP CREATE;Identifier;Name` SHALL create an empty group with a unique identifier and a name; a duplicate identifier SHALL be rejected.

#### Scenario: Create group
- **WHEN** `GROUP CREATE;G01;Familia Garcia` is processed and no group G01 exists
- **THEN** an empty group G01 named `Familia Garcia` exists

#### Scenario: Duplicate identifier
- **WHEN** `GROUP CREATE;G01;Otro` is processed and G01 already exists
- **THEN** an error is reported and the existing group is unchanged

### Requirement: Group membership
`GROUP ADD;Identifier;Email` SHALL add an existing registered user to an existing group; a user SHALL NOT appear more than once in the same group. `GROUP REMOVE;Identifier;Email` SHALL remove a member from the group.

#### Scenario: Add member once
- **WHEN** `GROUP ADD;G01;ana@correo.com` is processed twice
- **THEN** Ana is a member once and the second addition is rejected

#### Scenario: Unknown group or user
- **WHEN** `GROUP ADD` names a nonexistent group or an email of no registered user
- **THEN** an error is reported and no group changes

#### Scenario: Remove member
- **WHEN** `GROUP REMOVE;G01;ana@correo.com` is processed for a member of an unlocked group
- **THEN** Ana is no longer a member of G01

### Requirement: Group lock derived from active group tickets
A group SHALL be locked while at least one group ticket associated with it has a date equal to or later than the current system date. `GROUP ADD`, `GROUP REMOVE` and `GROUP DELETE` SHALL be rejected for a locked group. The lock SHALL NOT be stored as an attribute; it SHALL be derived from the existing tickets and the current date, so historical tickets do not lock and deleting the last future ticket unlocks the group.

#### Scenario: Active ticket locks the group
- **WHEN** G01 has a group ticket dated today or later and `GROUP ADD`, `GROUP REMOVE` or `GROUP DELETE` is processed for G01
- **THEN** the operation is rejected and G01 is unchanged

#### Scenario: Historical ticket does not lock
- **WHEN** the only group ticket of G01 is dated before the current system date
- **THEN** G01 membership can be changed and G01 can be deleted

#### Scenario: Deleting the future ticket unlocks
- **WHEN** the only active group ticket of G01 is a future ticket and it is deleted with `GROUPTICKET DELETE`
- **THEN** G01 can be modified again

### Requirement: Group deletion keeps historical tickets
`GROUP DELETE;Identifier` SHALL delete an unlocked group. Historical group tickets of the deleted group SHALL be retained with their identifier, date, price and composition, and their reference to the group SHALL become empty.

#### Scenario: Delete group with historical ticket
- **WHEN** G01 has only historical group tickets and `GROUP DELETE;G01` is processed
- **THEN** G01 no longer exists and its historical group tickets still exist without a group reference
