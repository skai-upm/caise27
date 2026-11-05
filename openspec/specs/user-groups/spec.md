# user-groups Specification

## Purpose
Lets registered users be organized into groups identified by a unique identifier and a name, with controlled membership changes and deletion that respect the group's active group tickets.

## Requirements

### Requirement: Group creation
`GROUP CREATE;Identifier;Name` SHALL create an empty group with the given identifier and name and print a response starting with `OK`. The identifier and name MUST be non-empty and the identifier MUST NOT belong to an existing group; otherwise the command is rejected with an `ERROR` response and no group is created.

#### Scenario: Empty group created
- **WHEN** `GROUP CREATE;G01;Familia Garcia` is processed
- **THEN** a response starting with `OK` is printed and group `G01` named `Familia Garcia` exists with no members

#### Scenario: Duplicate identifier rejected
- **WHEN** group `G01` exists and `GROUP CREATE;G01;Otro` is processed
- **THEN** an `ERROR` response is printed and group `G01` keeps its name

#### Scenario: Malformed creation rejected
- **WHEN** `GROUP CREATE;;Nombre`, `GROUP CREATE;G02;` or `GROUP CREATE;G02` is processed
- **THEN** an `ERROR` response is printed and no group is created

### Requirement: Adding users to a group
`GROUP ADD;Identifier;Email` SHALL add the existing user with that email to the existing group and print a response starting with `OK`. It MUST be rejected with an `ERROR` response, leaving the group unchanged, when the group does not exist, no user has the email, the user already belongs to the group, or the group has an active group ticket.

#### Scenario: Users added progressively
- **WHEN** users `ana@correo.com` and `pedro@correo.com` exist and `GROUP ADD;G01;ana@correo.com` then `GROUP ADD;G01;pedro@correo.com` are processed
- **THEN** both responses start with `OK` and group `G01` has two members

#### Scenario: User not added twice
- **WHEN** `ana@correo.com` already belongs to `G01` and `GROUP ADD;G01;ana@correo.com` is processed
- **THEN** an `ERROR` response is printed and `G01` still contains her only once

#### Scenario: Unknown group or user
- **WHEN** `GROUP ADD;G99;ana@correo.com` or `GROUP ADD;G01;nadie@correo.com` is processed
- **THEN** an `ERROR` response is printed and no membership changes

### Requirement: Removing users from a group
`GROUP REMOVE;Identifier;Email` SHALL remove the member with that email from the group and print a response starting with `OK`. It MUST be rejected with an `ERROR` response when the group does not exist, the email does not identify a member of the group, or the group has an active group ticket.

#### Scenario: Member removed
- **WHEN** `ana@correo.com` belongs to `G01` and `GROUP REMOVE;G01;ana@correo.com` is processed
- **THEN** a response starting with `OK` is printed and she is no longer a member

#### Scenario: Non-member removal rejected
- **WHEN** `GROUP REMOVE;G01;luis@correo.com` is processed and Luis is not a member of `G01`
- **THEN** an `ERROR` response is printed

### Requirement: Group deletion
`GROUP DELETE;Identifier` SHALL delete the group and print a response starting with `OK` only when no group ticket associated with the group has a date equal to or later than the current system date; group tickets for earlier dates do not prevent deletion. Deleting an unknown group or a group with such a ticket MUST be rejected with an `ERROR` response.

#### Scenario: Group without tickets deleted
- **WHEN** group `G01` has no group tickets and `GROUP DELETE;G01` is processed
- **THEN** a response starting with `OK` is printed and `G01` no longer exists

#### Scenario: Unknown group
- **WHEN** `GROUP DELETE;G99` is processed and no such group exists
- **THEN** an `ERROR` response is printed

### Requirement: Lock derived from active group tickets
A group SHALL be considered to have an active group ticket when at least one associated group ticket has a date equal to or later than the current system date. This state MUST be derived from the existing group tickets and the current system date each time it is needed and MUST NOT be stored as an attribute of the group. While no group tickets exist, every group is modifiable and deletable.

#### Scenario: No tickets means modifiable
- **WHEN** a group has no associated group tickets
- **THEN** `GROUP ADD`, `GROUP REMOVE` and `GROUP DELETE` are not rejected because of tickets

### Requirement: Active group ticket locks the group
While a group has an associated group ticket dated on or after the current system date, `GROUP ADD` and `GROUP REMOVE` on it MUST be rejected with an `ERROR` response, `GROUP DELETE` MUST be rejected, and `USER DELETE` of any of its members MUST be rejected. When all its group tickets are historical or have been deleted, the group SHALL become modifiable again.

#### Scenario: Locked by a future group ticket
- **WHEN** group `G01` has a group ticket dated after the system date
- **THEN** `GROUP ADD`, `GROUP REMOVE` and `GROUP DELETE` on `G01` are rejected and a member's `USER DELETE` is rejected

#### Scenario: Unlocked after deleting the future group ticket
- **WHEN** the only active group ticket of `G01` is deleted with `GROUPTICKET DELETE`
- **THEN** `GROUP ADD` on `G01` succeeds

#### Scenario: Today's ticket keeps the lock, history does not
- **WHEN** `G01`'s group ticket is dated today
- **THEN** `GROUP DELETE;G01` is rejected
- **AND** once the system date moves past that date, `GROUP DELETE;G01` succeeds

### Requirement: Deleted groups keep historical group tickets
Deleting a group SHALL keep its historical group tickets as records, including identifier, date, price and composition, with their reference to the group cleared.

#### Scenario: Historical group ticket survives group deletion
- **WHEN** `G01` has only a historical group ticket `TG01` and `GROUP DELETE;G01` succeeds
- **THEN** `TG01` and its composition still exist and `TG01` no longer references a group
