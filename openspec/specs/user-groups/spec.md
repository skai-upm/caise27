# user-groups Specification

## Purpose

Defines groups of registered users, how their membership is managed, and how active group tickets lock a group against modification and deletion.

## Requirements

### Requirement: Group creation
`GROUP CREATE;Identifier;Name` SHALL create an empty group with a unique identifier and a name. A duplicate identifier SHALL be rejected.

#### Scenario: Group created empty
- **WHEN** `GROUP CREATE;G01;Familia Garcia` is processed
- **THEN** a confirmation is printed and group G01 exists with no members

#### Scenario: Duplicate identifier
- **WHEN** group G01 exists and `GROUP CREATE;G01;Otro` is processed
- **THEN** an `ERROR:` message is printed

### Requirement: Adding members
`GROUP ADD;Identifier;Email` SHALL add an existing user to an existing group. It SHALL be rejected when the group or user does not exist, the user already belongs to the group, or the group is locked.

#### Scenario: Members added progressively
- **WHEN** `GROUP ADD;G01;ana@correo.com` and `GROUP ADD;G01;pedro@correo.com` are processed for existing users
- **THEN** G01 contains both users

#### Scenario: Duplicate member
- **WHEN** ana already belongs to G01 and `GROUP ADD;G01;ana@correo.com` is processed
- **THEN** an `ERROR:` message is printed and ana appears once

### Requirement: Removing members
`GROUP REMOVE;Identifier;Email` SHALL remove a member from a group. It SHALL be rejected when the group does not exist, the user is not a member, or the group is locked.

#### Scenario: Member removed
- **WHEN** ana belongs to unlocked G01 and `GROUP REMOVE;G01;ana@correo.com` is processed
- **THEN** ana is no longer a member of G01

### Requirement: Derived group lock
A group SHALL be locked while at least one group ticket bought by it has a date equal to or later than the current system date. The lock SHALL be derived from tickets and the current date, not stored. Historical group tickets do not lock.

#### Scenario: Locked by future ticket
- **WHEN** G01 has a group ticket dated after the current date and `GROUP ADD` or `GROUP REMOVE` is processed for G01
- **THEN** an `ERROR:` message is printed and membership is unchanged

#### Scenario: Unlocked when ticket becomes historical
- **WHEN** the system date is moved past the date of G01's only group ticket
- **THEN** GROUP ADD and GROUP REMOVE on G01 succeed again

#### Scenario: Unlocked after deleting the future ticket
- **WHEN** G01's only active group ticket is a future one and it is deleted with GROUPTICKET DELETE
- **THEN** G01 can be modified again

### Requirement: Group deletion
`GROUP DELETE;Identifier` SHALL delete a group only when it has no group ticket dated on or after the current system date. Its historical group tickets SHALL be kept with their identifier, date, price and composition, and their group reference becomes null.

#### Scenario: Deletion blocked by current-day ticket
- **WHEN** G01 has a group ticket dated on the current date and `GROUP DELETE;G01` is processed
- **THEN** an `ERROR:` message is printed and G01 still exists

#### Scenario: Deletion keeps historical tickets
- **WHEN** G01 only has group tickets dated before the current date and `GROUP DELETE;G01` is processed
- **THEN** G01 no longer exists and its historical group tickets still exist with their composition and price and no group
