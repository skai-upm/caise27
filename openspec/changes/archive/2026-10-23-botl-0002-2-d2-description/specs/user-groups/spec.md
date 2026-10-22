# Spec Delta

## Purpose

Lets registered users be organized into named groups that can buy group tickets, and protects a group's composition while it has active group tickets.

## ADDED Requirements

### Requirement: Group creation
`GROUP CREATE;<identifier>;<name>` SHALL create an empty group with a unique, non-empty identifier and a non-empty name, persist it, and print `GROUP CREATED`. A duplicate identifier or empty field SHALL be rejected with an error beginning with `ERROR`.

#### Scenario: Create group
- **WHEN** `GROUP CREATE;G01;Familia Garcia` is processed and no group `G01` exists
- **THEN** `GROUP CREATED` is printed and group `G01` exists with no members

#### Scenario: Duplicate identifier
- **WHEN** `GROUP CREATE;G01;Otro` is processed and `G01` already exists
- **THEN** an error is printed and the existing group is unchanged

### Requirement: Adding members
`GROUP ADD;<identifier>;<email>` SHALL add an existing user to an existing group, persist the change, and print `USER ADDED TO GROUP`. It SHALL be rejected with an error if the group or user does not exist, the user is already a member, or the group is locked.

#### Scenario: Add user
- **WHEN** `GROUP ADD;G01;ana@correo.com` is processed for an unlocked group and an existing user who is not a member
- **THEN** the confirmation is printed and Ana is a member of `G01`

#### Scenario: Duplicate member rejected
- **WHEN** `GROUP ADD;G01;ana@correo.com` is processed and Ana is already a member of `G01`
- **THEN** an error is printed and Ana appears only once in `G01`

### Requirement: Removing members
`GROUP REMOVE;<identifier>;<email>` SHALL remove a member from an existing, unlocked group, persist the change, and print `USER REMOVED FROM GROUP`. It SHALL be rejected with an error if the group does not exist, the user is not a member, or the group is locked.

#### Scenario: Remove user
- **WHEN** `GROUP REMOVE;G02;laura@correo.com` is processed, Laura is a member, and `G02` is unlocked
- **THEN** the confirmation is printed and Laura is no longer a member

### Requirement: Group lock derived from active group tickets
A group SHALL be locked exactly when at least one group ticket bought by it has a date on or after the current system date. The lock SHALL be computed from the existing tickets and the current date, not stored. `GROUP ADD`, `GROUP REMOVE`, and `GROUP DELETE` SHALL be rejected while the group is locked.

#### Scenario: Locked by active ticket
- **WHEN** group `G01` has a group ticket dated on or after the current date and `GROUP ADD;G01;marta@correo.com` is processed
- **THEN** an error is printed and Marta is not added

#### Scenario: Unlocked after future ticket deleted
- **WHEN** the only active group ticket of `G02` is deleted with `GROUPTICKET DELETE`
- **THEN** `GROUP REMOVE` and `GROUP ADD` on `G02` are accepted again

#### Scenario: Historical tickets do not lock
- **WHEN** all group tickets of `G01` are dated before the current date
- **THEN** `G01` can be modified and deleted

### Requirement: Group deletion
`GROUP DELETE;<identifier>` SHALL delete an existing, unlocked group, persist the change, and print `GROUP DELETED`. Historical group tickets bought by the deleted group SHALL be kept and SHALL no longer reference the group.

#### Scenario: Delete group
- **WHEN** `GROUP DELETE;G02` is processed and `G02` has no active group ticket
- **THEN** `GROUP DELETED` is printed and `G02` no longer exists

#### Scenario: Locked group cannot be deleted
- **WHEN** `GROUP DELETE;G01` is processed and `G01` has a group ticket dated today
- **THEN** an error is printed and `G01` still exists
