# user-groups Specification

## Purpose

Defines user groups: identified, named collections of registered users that can be created empty and whose membership can be modified, as the basis for group tickets.

## Requirements

### Requirement: GROUP CREATE command
`GROUP CREATE;Identifier;Name` SHALL create an empty group with the given identifier and name and print `GROUP CREATED`. Group identifiers SHALL be unique; an existing identifier or an empty identifier or name MUST be rejected with an error message starting with `ERROR`.

#### Scenario: Empty group created
- **WHEN** `GROUP CREATE;G01;Familia Garcia` is processed
- **THEN** `GROUP CREATED` is printed and group `G01` named `Familia Garcia` exists with no members

#### Scenario: Duplicate identifier rejected
- **GIVEN** group `G01` exists
- **WHEN** `GROUP CREATE;G01;Otro` is processed
- **THEN** an error message starting with `ERROR` is printed and `G01` keeps the name `Familia Garcia`

### Requirement: GROUP ADD command
`GROUP ADD;Identifier;Email` SHALL add the registered user with that email to the group and print `USER ADDED TO GROUP`. It MUST be rejected with an error message starting with `ERROR`, leaving the group unchanged, when the group does not exist, no user has that email, or the user already belongs to that group.

#### Scenario: Users added progressively
- **GIVEN** an empty group `G01` and registered users `ana@correo.com` and `pedro@correo.com`
- **WHEN** `GROUP ADD;G01;ana@correo.com` and `GROUP ADD;G01;pedro@correo.com` are processed
- **THEN** both print `USER ADDED TO GROUP` and `G01` has the two users as members

#### Scenario: User cannot be added twice
- **GIVEN** `ana@correo.com` belongs to `G01`
- **WHEN** `GROUP ADD;G01;ana@correo.com` is processed
- **THEN** an error message starting with `ERROR` is printed and Ana appears once in `G01`

#### Scenario: Unknown group or user rejected
- **WHEN** `GROUP ADD;G99;ana@correo.com` or `GROUP ADD;G01;nadie@correo.com` is processed
- **THEN** an error message starting with `ERROR` is printed and no membership changes

#### Scenario: Same user in different groups
- **GIVEN** groups `G01` and `G02` and Ana a member of `G01`
- **WHEN** `GROUP ADD;G02;ana@correo.com` is processed
- **THEN** `USER ADDED TO GROUP` is printed

### Requirement: GROUP REMOVE command
`GROUP REMOVE;Identifier;Email` SHALL remove the user with that email from the group and print `USER REMOVED FROM GROUP`. It MUST be rejected with an error message starting with `ERROR` when the group does not exist or the user is not a member of it.

#### Scenario: Member removed
- **GIVEN** Ana belongs to `G01`
- **WHEN** `GROUP REMOVE;G01;ana@correo.com` is processed
- **THEN** `USER REMOVED FROM GROUP` is printed and Ana is no longer a member of `G01`

#### Scenario: Non-member removal rejected
- **GIVEN** Pedro does not belong to `G01`
- **WHEN** `GROUP REMOVE;G01;pedro@correo.com` is processed
- **THEN** an error message starting with `ERROR` is printed

### Requirement: Groups locked by active group tickets
A group SHALL be considered locked while at least one of its group tickets is active (date equal to or later than the current system date); the locked state is derived from the tickets and the date, not stored. `GROUP ADD` and `GROUP REMOVE` on a locked group MUST be rejected with an error message starting with `ERROR`. Historical group tickets do not lock the group.

#### Scenario: Active group ticket locks membership
- **GIVEN** group `G01` with an active group ticket
- **WHEN** `GROUP ADD;G01;luis@correo.com` or `GROUP REMOVE;G01;ana@correo.com` is processed
- **THEN** an error message starting with `ERROR` is printed and the membership is unchanged

#### Scenario: Group modifiable again after ticket deleted or historical
- **GIVEN** group `G01` whose only group ticket was deleted with `GROUPTICKET DELETE` or is dated before the current date
- **WHEN** `GROUP ADD;G01;luis@correo.com` is processed
- **THEN** `USER ADDED TO GROUP` is printed

### Requirement: GROUP DELETE command
`GROUP DELETE;<identifier>` SHALL delete the group and print `GROUP DELETED` only when the group has no active group ticket; otherwise it is rejected with an error message starting with `ERROR`. Historical group tickets of a deleted group SHALL be retained with their identifier, date, price and composition, and their group reference becomes empty.

#### Scenario: Group with only historical tickets deleted
- **GIVEN** group `G01` whose group ticket `TG01` is dated before the current date
- **WHEN** `GROUP DELETE;G01` is processed
- **THEN** `GROUP DELETED` is printed, `G01` no longer exists and `TG01` is retained with its composition and price but no group reference

#### Scenario: Group with an active ticket not deleted
- **GIVEN** group `G01` with a group ticket dated today
- **WHEN** `GROUP DELETE;G01` is processed
- **THEN** an error message starting with `ERROR` is printed and `G01` still exists

#### Scenario: Unknown group
- **WHEN** `GROUP DELETE;G99` is processed
- **THEN** an error message starting with `ERROR` is printed
