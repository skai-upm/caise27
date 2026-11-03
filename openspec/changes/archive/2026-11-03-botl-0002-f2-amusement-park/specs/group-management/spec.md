# Spec Delta

## Purpose

Lets registered users be organized into named groups whose composition can change only while no active group ticket locks them (source F2-C02 and F2-C04, section 5).

## ADDED Requirements

### Requirement: Create group
`GROUP CREATE;Identifier;Name` SHALL create an empty group with a unique identifier and the given name; an identifier already used by another group SHALL be rejected.

#### Scenario: Empty group created
- **WHEN** `GROUP CREATE;G01;Familia Garcia` is processed and no group `G01` exists
- **THEN** group `G01` named `Familia Garcia` exists with no members

#### Scenario: Duplicate identifier rejected
- **WHEN** `GROUP CREATE;G01;Otro` is processed and group `G01` already exists
- **THEN** an error is reported and the existing group is unchanged

### Requirement: Add user to group
`GROUP ADD;Identifier;Email` SHALL add an existing user to an existing group; it SHALL reject an unknown group, an unknown user, a user who already belongs to that group, or a locked group.

#### Scenario: Member added once
- **WHEN** `GROUP ADD;G01;ana@correo.com` is processed twice for an unlocked group and registered user
- **THEN** the first adds Ana as a member and the second is rejected, leaving Ana in the group exactly once

#### Scenario: Unknown user rejected
- **WHEN** `GROUP ADD;G01;nadie@correo.com` is processed and no user has that email
- **THEN** an error is reported and the group is unchanged

### Requirement: Remove user from group
`GROUP REMOVE;Identifier;Email` SHALL remove a member from an existing unlocked group; it SHALL reject an unknown group, a user who is not a member, or a locked group.

#### Scenario: Member removed
- **WHEN** `GROUP REMOVE;G01;ana@correo.com` is processed for an unlocked group containing Ana
- **THEN** Ana is no longer a member

### Requirement: Delete group
`GROUP DELETE;Identifier` SHALL delete an existing group only when it has no associated group ticket dated on or after the current system date; associated historical group tickets SHALL be retained with their group reference cleared.

#### Scenario: Group with a ticket for today cannot be deleted
- **WHEN** group `G01` has a group ticket dated today and `GROUP DELETE;G01` is processed
- **THEN** an error is reported and the group still exists

#### Scenario: Group with only historical tickets deleted
- **WHEN** group `G01` only has group tickets dated before the current date and `GROUP DELETE;G01` is processed
- **THEN** the group is deleted and its historical group tickets remain with identifier, date, price and composition

### Requirement: Group lock is derived from tickets
A group SHALL be locked exactly when at least one associated group ticket has a date equal to or later than the current system date; the lock SHALL NOT be stored as an independent attribute and SHALL be re-evaluated on every check.

#### Scenario: Lock released when the future ticket is deleted
- **WHEN** group `G01`'s only active group ticket is a future ticket that is deleted with `GROUPTICKET DELETE`
- **THEN** `GROUP ADD` and `GROUP REMOVE` on `G01` are accepted again

#### Scenario: Lock released when the date passes
- **WHEN** group `G01`'s only group ticket is dated 2027-04-15 and `SYSTEM DATE;2027-04-16` is processed
- **THEN** `G01` is no longer locked
