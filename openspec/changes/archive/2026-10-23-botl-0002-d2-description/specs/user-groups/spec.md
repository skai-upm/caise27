# Spec Delta

## Purpose

Lets registered users be organised into named groups that can buy group tickets, and protects a group's composition while it has active group tickets.

## ADDED Requirements

### Requirement: Group creation
`GROUP CREATE;Identifier;Name` SHALL create an empty group with a non-empty identifier, unique among groups (compared ignoring case), and a non-empty name.

#### Scenario: Create group
- **WHEN** `GROUP CREATE;G01;Familia Garcia` is processed and no group G01 exists
- **THEN** `GROUP CREATED` is printed and an empty group G01 exists

#### Scenario: Duplicate identifier
- **WHEN** `GROUP CREATE;G01;Otro` is processed and group G01 exists
- **THEN** an error is printed and no group is created

### Requirement: Group lock derived from active group tickets
A group SHALL be locked while at least one group ticket bought by it has a date on or after the current system date. The lock SHALL be computed from existing tickets and the system date, never stored. Group tickets with earlier dates are historical and do not lock the group.

#### Scenario: Lock released by date change
- **WHEN** group G01 has only a group ticket dated 2027-04-15 and the system date changes from 2027-04-15 to 2027-04-16
- **THEN** G01 is no longer locked

### Requirement: Adding members
`GROUP ADD;Identifier;Email` SHALL add an existing user to an existing group, unless the user already belongs to that group or the group is locked. A user MAY belong to several groups.

#### Scenario: Add member
- **WHEN** `GROUP ADD;G01;ana@correo.com` is processed and Ana exists and is not in G01
- **THEN** `USER ADDED TO GROUP` is printed and Ana is a member of G01

#### Scenario: Duplicate member
- **WHEN** `GROUP ADD;G01;ana@correo.com` is processed and Ana is already in G01
- **THEN** an error is printed and the membership is unchanged

#### Scenario: Unknown user or group
- **WHEN** the group or the user does not exist
- **THEN** an error is printed

#### Scenario: Locked group
- **WHEN** G01 has an active group ticket and `GROUP ADD;G01;laura@correo.com` is processed
- **THEN** an error stating the group is locked by an active group ticket is printed

### Requirement: Removing members
`GROUP REMOVE;Identifier;Email` SHALL remove a member from an existing group unless the group is locked; removing a user who is not a member SHALL be rejected.

#### Scenario: Remove member
- **WHEN** `GROUP REMOVE;G02;laura@correo.com` is processed, Laura is in G02 and G02 is not locked
- **THEN** `USER REMOVED FROM GROUP` is printed and Laura is no longer in G02

#### Scenario: Remove from locked group
- **WHEN** G01 has an active group ticket and `GROUP REMOVE;G01;ana@correo.com` is processed
- **THEN** an error is printed and Ana stays in G01

### Requirement: Group deletion
`GROUP DELETE;Identifier` SHALL delete an existing group only if it is not locked. Historical group tickets of the deleted group SHALL be kept with their identifier, date, price and composition, and lose their reference to the group.

#### Scenario: Delete unlocked group
- **WHEN** `GROUP DELETE;G02` is processed and G02 has no active group ticket
- **THEN** `GROUP DELETED` is printed and G02 no longer exists

#### Scenario: Delete locked group
- **WHEN** G01 has a group ticket dated on or after the system date and `GROUP DELETE;G01` is processed
- **THEN** an error is printed and G01 still exists

#### Scenario: Historical tickets survive group deletion
- **WHEN** G01 only has a group ticket dated before the system date and G01 is deleted
- **THEN** the group ticket still exists with its members and price, and no longer refers to a group
