# user-groups Specification

## Purpose

Lets registered users be organised into identified, named groups whose membership is locked while the group holds an active group ticket.

## Requirements

### Requirement: Create group
`GROUP CREATE;Identifier;Name` SHALL create an empty group with a unique, non-empty identifier and the given name.

#### Scenario: Create empty group
- **WHEN** `GROUP CREATE;G01;Familia Garcia` is processed
- **THEN** group `G01` exists with name `Familia Garcia` and no members

#### Scenario: Duplicate identifier
- **WHEN** group `G01` exists and `GROUP CREATE;G01;Otro` is processed
- **THEN** an error is reported and the existing group is unchanged

### Requirement: Add member
`GROUP ADD;Identifier;Email` SHALL add an existing user to an existing group. A user SHALL NOT appear more than once in the same group.

#### Scenario: Add users
- **WHEN** users `ana@correo.com` and `pedro@correo.com` exist and both are added to `G01`
- **THEN** `G01` has both users as members

#### Scenario: Duplicate member
- **WHEN** `ana@correo.com` is already in `G01` and `GROUP ADD;G01;ana@correo.com` is processed
- **THEN** an error is reported and the user appears once

#### Scenario: Unknown group or user
- **WHEN** `GROUP ADD` names a group or email that does not exist
- **THEN** an error is reported and no group changes

### Requirement: Remove member
`GROUP REMOVE;Identifier;Email` SHALL remove a member from a group.

#### Scenario: Remove member
- **WHEN** `ana@correo.com` is in unlocked group `G01` and `GROUP REMOVE;G01;ana@correo.com` is processed
- **THEN** Ana is no longer a member of `G01`

#### Scenario: Not a member
- **WHEN** `GROUP REMOVE` names a user who is not in the group
- **THEN** an error is reported

### Requirement: Derived group lock
A group SHALL be locked while at least one group ticket associated with it has a date on or after the current system date. The lock SHALL be derived from tickets and the system date, never stored. `GROUP ADD` and `GROUP REMOVE` SHALL be rejected while the group is locked.

#### Scenario: Locked by active group ticket
- **WHEN** group `G01` has a group ticket dated 2027-04-15 and the system date is 2027-04-15
- **THEN** `GROUP ADD` and `GROUP REMOVE` on `G01` are rejected

#### Scenario: Historical ticket does not lock
- **WHEN** the only group ticket of `G01` is dated 2027-04-15 and the system date is 2027-04-16
- **THEN** `GROUP ADD` and `GROUP REMOVE` on `G01` are allowed

#### Scenario: Unlocked after deleting future ticket
- **WHEN** the only active group ticket of `G01` is in the future and is deleted with `GROUPTICKET DELETE`
- **THEN** `G01` can be modified again

### Requirement: Delete group
`GROUP DELETE;Identifier` SHALL delete a group only if it has no group ticket dated on or after the current system date. Historical group tickets SHALL be retained with their identifier, date, price and composition, and their group reference SHALL become empty.

#### Scenario: Delete group without active tickets
- **WHEN** `G01` has only historical group tickets and `GROUP DELETE;G01` is processed
- **THEN** `G01` no longer exists and its historical group tickets still exist without a group

#### Scenario: Active ticket blocks deletion
- **WHEN** `G01` has a group ticket dated today or later
- **THEN** `GROUP DELETE;G01` is rejected and the group still exists
