# Spec Delta

## Purpose

Defines user groups: their creation with a unique identifier and name, the management of their members (existing users, no duplicates), their deletion, and the ticket-derived rules that lock them.

## ADDED Requirements

### Requirement: Create group
The command `GROUP CREATE;Identifier;Name` SHALL create an empty group with the given identifier and name. The identifier SHALL be unique among existing groups; a duplicate identifier, a blank identifier or name, or a wrong number of arguments SHALL be rejected with an error message.

#### Scenario: Create an empty group
- **WHEN** `GROUP CREATE;G01;Familia Garcia` is processed and no group `G01` exists
- **THEN** a confirmation is shown and group `G01` named `Familia Garcia` exists with no members

#### Scenario: Duplicate identifier rejected
- **WHEN** `GROUP CREATE;G01;Otro` is processed and group `G01` already exists
- **THEN** an error message is shown and the existing group is unchanged

### Requirement: Add user to group
The command `GROUP ADD;Identifier;Email` SHALL add the existing user with that email to the group. It SHALL be rejected with an error message when the group does not exist, no user with that email exists in the system, the user already belongs to the group, or the group has an active group ticket.

#### Scenario: Add existing users progressively
- **WHEN** users `ana@correo.com` and `pedro@correo.com` exist, group `G01` exists, and `GROUP ADD;G01;ana@correo.com` then `GROUP ADD;G01;pedro@correo.com` are processed
- **THEN** both are confirmed and `G01` contains both users

#### Scenario: Same user twice rejected
- **WHEN** `ana@correo.com` already belongs to `G01` and `GROUP ADD;G01;ana@correo.com` is processed
- **THEN** an error message is shown and `ana@correo.com` appears only once in `G01`

#### Scenario: Unknown user or group rejected
- **WHEN** `GROUP ADD;G01;nadie@correo.com` is processed and no such user exists, or `GROUP ADD;G99;ana@correo.com` is processed and no group `G99` exists
- **THEN** an error message is shown and no group membership changes

### Requirement: Remove user from group
The command `GROUP REMOVE;Identifier;Email` SHALL remove the user with that email from the group. It SHALL be rejected with an error message when the group does not exist, the user is not a member of the group, or the group has an active group ticket.

#### Scenario: Remove a member
- **WHEN** `ana@correo.com` belongs to `G01` and `GROUP REMOVE;G01;ana@correo.com` is processed
- **THEN** a confirmation is shown and `ana@correo.com` no longer belongs to `G01`

#### Scenario: Remove a non-member rejected
- **WHEN** `pedro@correo.com` does not belong to `G01` and `GROUP REMOVE;G01;pedro@correo.com` is processed
- **THEN** an error message is shown

### Requirement: Delete group
The command `GROUP DELETE;Identifier` SHALL delete the group. Deletion SHALL be rejected with an error message when the group does not exist or when an associated group ticket has a date equal to or later than the current system date; group tickets for earlier dates SHALL NOT prevent deletion.

#### Scenario: Delete a group without tickets
- **WHEN** group `G01` has no associated group tickets and `GROUP DELETE;G01` is processed
- **THEN** a confirmation is shown and `G01` no longer exists

#### Scenario: Delete unknown group rejected
- **WHEN** `GROUP DELETE;G99` is processed and no group `G99` exists
- **THEN** an error message is shown

### Requirement: Group lock derived from tickets
A group SHALL be considered to have an active group ticket when at least one associated group ticket has a date equal to or later than the current system date. This locked state SHALL be computed from the existing tickets and the current system date and SHALL NOT be stored as an independent attribute of the group; tickets for earlier dates are historical and SHALL NOT lock the group.

#### Scenario: Group with no tickets is modifiable
- **WHEN** a group has no associated group tickets
- **THEN** GROUP ADD, GROUP REMOVE and GROUP DELETE are not blocked by the lock rule

#### Scenario: Lock re-evaluated with the current date
- **WHEN** the current system date changes
- **THEN** whether the group is locked is evaluated again from its tickets and the new date
