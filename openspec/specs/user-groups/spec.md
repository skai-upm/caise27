# user-groups Specification

## Purpose

Lets registered users of the Amusement Park be organized into identified, named groups whose membership can change only while no active group ticket exists for them.

## Requirements

### Requirement: Create group
`GROUP CREATE;Identifier;Name` SHALL create an empty group with that unique identifier and name. A non-empty identifier and name are required; an identifier already used by another group MUST be rejected with an error and change nothing.

#### Scenario: Empty group created
- **WHEN** `GROUP CREATE;G01;Familia Garcia` is processed
- **THEN** a confirmation is printed and group `G01` named `Familia Garcia` exists with no members

#### Scenario: Duplicate identifier rejected
- **GIVEN** group `G01` exists
- **WHEN** `GROUP CREATE;G01;Otro` is processed
- **THEN** an error reporting that the group already exists is printed and `G01` keeps its name

### Requirement: Add user to group
`GROUP ADD;Identifier;Email` SHALL add the existing user with that email to the group. It MUST be rejected with an error, changing nothing, when the group does not exist, no user has that email, the user already belongs to that group, or the group is locked.

#### Scenario: Existing users added
- **GIVEN** group `G01` and users `ana@correo.com` and `pedro@correo.com`
- **WHEN** `GROUP ADD;G01;ana@correo.com` and `GROUP ADD;G01;pedro@correo.com` are processed
- **THEN** both users are members of `G01`

#### Scenario: User added twice rejected
- **GIVEN** `ana@correo.com` is a member of `G01`
- **WHEN** `GROUP ADD;G01;ana@correo.com` is processed
- **THEN** an error reporting that the user is already in the group is printed and the user appears once

#### Scenario: Unknown group or user rejected
- **WHEN** `GROUP ADD;G99;ana@correo.com` or `GROUP ADD;G01;nadie@correo.com` is processed
- **THEN** an error naming the missing group or user is printed

### Requirement: Remove user from group
`GROUP REMOVE;Identifier;Email` SHALL remove that user from the group while the group is modifiable; it MUST be rejected with an error when the group does not exist, the user is not a member, or the group is locked.

#### Scenario: Member removed
- **GIVEN** `ana@correo.com` is a member of `G01`
- **WHEN** `GROUP REMOVE;G01;ana@correo.com` is processed
- **THEN** `ana@correo.com` is no longer a member of `G01`

#### Scenario: Non-member rejected
- **WHEN** `GROUP REMOVE;G01;pedro@correo.com` is processed and Pedro is not a member
- **THEN** an error reporting that the user is not in the group is printed

### Requirement: Locked state derived from active group tickets
A group SHALL be locked exactly when at least one associated group ticket has a date equal to or later than the current system date; group tickets for earlier dates MUST NOT lock it. The locked state MUST be derived from the existing tickets and the current system date, never stored. While locked, `GROUP ADD` and `GROUP REMOVE` MUST be rejected with an error and change nothing.

#### Scenario: Group with an active ticket cannot change membership
- **GIVEN** group `G01` has an associated group ticket dated on or after the current system date
- **WHEN** `GROUP ADD;G01;pedro@correo.com` or `GROUP REMOVE;G01;ana@correo.com` is processed
- **THEN** an error reporting that the group has an active group ticket is printed and the members are unchanged

#### Scenario: Only historical tickets
- **GIVEN** every group ticket of `G01` is dated before the current system date
- **WHEN** `GROUP ADD;G01;pedro@correo.com` is processed
- **THEN** the user is added

### Requirement: Delete group
`GROUP DELETE;Identifier` SHALL delete the group only when it has no associated group ticket dated on or after the current system date; otherwise, or when the group does not exist, it MUST be rejected with an error and the group kept.

#### Scenario: Group without active tickets deleted
- **GIVEN** group `G01` with no active group ticket
- **WHEN** `GROUP DELETE;G01` is processed
- **THEN** a confirmation is printed and `G01` no longer exists

#### Scenario: Group with an active ticket kept
- **GIVEN** group `G01` has a group ticket for the current day
- **WHEN** `GROUP DELETE;G01` is processed
- **THEN** an error reporting the active group ticket is printed and `G01` still exists
