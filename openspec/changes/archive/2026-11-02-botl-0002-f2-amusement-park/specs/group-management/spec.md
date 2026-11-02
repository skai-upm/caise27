# Spec Delta

## Purpose

Lets registered users be organized into groups with a unique identifier and name, and keeps group composition locked while the group has an active group ticket.

## ADDED Requirements

### Requirement: Group creation
`GROUP CREATE;Identifier;Name` SHALL create an empty group whose identifier is not used by another group. (F2-C02)

#### Scenario: Create group
- **WHEN** `GROUP CREATE;G01;Familia Garcia` runs and no group G01 exists
- **THEN** an empty group G01 named `Familia Garcia` exists

#### Scenario: Duplicate identifier
- **WHEN** `GROUP CREATE;G01;Otro` runs and G01 already exists
- **THEN** an error is reported and the existing group is unchanged

### Requirement: Group membership changes
`GROUP ADD;Identifier;Email` SHALL add an existing registered user to an existing group, rejecting a user already in that group; `GROUP REMOVE;Identifier;Email` SHALL remove a member. Both SHALL be allowed only while the group has no active group ticket. (F2-C02)

#### Scenario: Add once
- **WHEN** `GROUP ADD;G01;ana@correo.com` runs twice on a modifiable group
- **THEN** Ana is a member once and the second command is rejected

#### Scenario: Unknown user or group
- **WHEN** `GROUP ADD` names an unregistered email or a nonexistent group
- **THEN** an error is reported and no group changes

#### Scenario: Remove member
- **WHEN** `GROUP REMOVE;G01;ana@correo.com` runs on a modifiable group containing Ana
- **THEN** Ana is no longer a member

### Requirement: Derived group lock
A group SHALL be locked exactly when at least one group ticket associated with it has a date equal to or later than the current system date; the locked state SHALL be derived from existing tickets and the system date, never stored. GROUP ADD, GROUP REMOVE and GROUP DELETE SHALL be rejected on a locked group. (F2-C02, F2-C04)

#### Scenario: Locked by today's ticket
- **WHEN** G01 has a group ticket dated today and GROUP ADD, GROUP REMOVE or GROUP DELETE is run on it
- **THEN** each command is rejected and G01 is unchanged

#### Scenario: Historical tickets do not lock
- **WHEN** the system date moves past every G01 group ticket date
- **THEN** G01 can be modified and deleted again

#### Scenario: Unlocked after deleting future ticket
- **WHEN** G01's only active group ticket is a future one and it is removed with GROUPTICKET DELETE
- **THEN** G01 can be modified again

### Requirement: Group deletion
`GROUP DELETE;Identifier` SHALL delete an unlocked group; historical group tickets of the deleted group SHALL be kept, with their group reference cleared. (F2-C02, F2-C04)

#### Scenario: Delete with only historical tickets
- **WHEN** G01 has only group tickets dated before today and `GROUP DELETE;G01` runs
- **THEN** G01 no longer exists and its historical group tickets remain with identifier, date, price and composition
