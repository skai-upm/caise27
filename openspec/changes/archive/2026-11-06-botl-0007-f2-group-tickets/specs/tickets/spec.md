# Spec Delta

## ADDED Requirements

### Requirement: Create a group ticket
The system SHALL create a group ticket with `GROUPTICKET CREATE;<identifier>;<group>;<date>` when the group exists and has at least one member, the date (yyyy-MM-dd) is not earlier than the current system date, and the identifier and every derived identifier are unused. The group ticket SHALL consist of one individual ticket per member at purchase time, identified `<identifier>:<dni>` and associated with that member. The confirmation SHALL display the total price.

#### Scenario: Group ticket with composed tickets
- **GIVEN** group `G01` with members Ana (DNI `12345678Z`) and Pedro (DNI `87654321X`), and system date 2027-04-15
- **WHEN** `GROUPTICKET CREATE;TG01;G01;2027-04-15` is processed
- **THEN** group ticket `TG01` exists for group `G01` dated 2027-04-15
- **AND** individual tickets `TG01:12345678Z` for Ana and `TG01:87654321X` for Pedro exist with the same date
- **AND** the confirmation displays the total price 80.00

#### Scenario: Empty group or past date is rejected
- **WHEN** `GROUPTICKET CREATE` names an empty group, an unknown group, a date before the system date, or an invalid date
- **THEN** an error message is displayed and no ticket is created

#### Scenario: Identifier conflict prevents partial creation
- **GIVEN** a ticket with identifier `TG01:12345678Z` already exists and Ana (DNI `12345678Z`) is a member of `G01`
- **WHEN** `GROUPTICKET CREATE;TG01;G01;2027-04-15` is processed
- **THEN** an error message is displayed
- **AND** neither `TG01` nor any other composed ticket of it is created

#### Scenario: Composition does not follow later group changes
- **GIVEN** group ticket `TG01` was created for `G01` with two members and is now historical
- **WHEN** a new member is added to `G01`
- **THEN** `TG01` still consists of the same two individual tickets

### Requirement: Group discount by member count
The system SHALL price each composed ticket at the current base price minus the discount for the number of members included: fewer than 3 → 0%, 3 to 5 → 10%, 6 to 10 → 20%, more than 10 → 50%. The group ticket total SHALL be the sum of its composed tickets. Later price changes SHALL NOT alter existing group tickets.

#### Scenario: Six members at 40 euros
- **GIVEN** the base price is 40.00 and group `G02` has 6 members
- **WHEN** `GROUPTICKET CREATE;TG02;G02;2027-04-15` is processed
- **THEN** each composed ticket costs 32.00
- **AND** the displayed total is 192.00

#### Scenario: Discount brackets
- **GIVEN** the base price is 40.00
- **WHEN** group tickets are created for groups of 2, 3, 5, 10 and 11 members
- **THEN** the per-ticket prices are 40.00, 36.00, 36.00, 32.00 and 20.00 respectively

### Requirement: Delete a group ticket
The system SHALL delete a group ticket with `GROUPTICKET DELETE;<identifier>` only when it exists and is dated strictly after the current system date, and SHALL delete its composed individual tickets with it. Group tickets for the current day or earlier SHALL be kept as historical records and the deletion rejected with an error message.

#### Scenario: Delete a future group ticket
- **GIVEN** system date 2027-04-15 and group ticket `TG01` dated 2027-04-20
- **WHEN** `GROUPTICKET DELETE;TG01` is processed
- **THEN** a confirmation is displayed and `TG01` and all its composed tickets no longer exist
- **AND** if no other active group ticket remains for its group, the group can be modified again

#### Scenario: Today's group ticket cannot be deleted
- **GIVEN** group ticket `TG01` is dated today
- **WHEN** `GROUPTICKET DELETE;TG01` is processed
- **THEN** an error message is displayed and `TG01` and its composed tickets remain

### Requirement: Group ticket references its group while it exists
A group ticket SHALL refer to the group that purchased it while that group exists, and the group SHALL NOT keep references to its tickets. When the group is deleted, its historical group tickets SHALL be kept with their identifier, date, price and composition, and their group reference SHALL become empty.

#### Scenario: Deleting a group with only historical group tickets
- **GIVEN** group `G01` has only group tickets dated before the current date
- **WHEN** `GROUP DELETE;G01` is processed
- **THEN** the group is deleted
- **AND** its group tickets still exist with their composition and price and no group reference

### Requirement: GROUPTICKET commands listed in HELP
The system SHALL include `GROUPTICKET CREATE;<identifier>;<group>;<date>` and `GROUPTICKET DELETE;<identifier>` in the output of `HELP`.

#### Scenario: HELP shows GROUPTICKET commands
- **WHEN** `HELP` is processed
- **THEN** the output contains both GROUPTICKET command syntaxes

## MODIFIED Requirements

### Requirement: Delete an individual ticket
The system SHALL delete an individual ticket with `TICKET DELETE;<identifier>` only when it exists, is not part of a group ticket, and its date is strictly later than the current system date. Tickets dated on or before the current system date SHALL be kept as historical records and the deletion rejected with an error message. Individual tickets that compose a group ticket SHALL only be deleted through deletion of their group ticket.

#### Scenario: Delete a future ticket
- **GIVEN** the system date is 2027-04-15 and ticket `T002` is dated 2027-04-16
- **WHEN** `TICKET DELETE;T002` is processed
- **THEN** a confirmation is displayed and `T002` no longer exists

#### Scenario: Ticket for today or earlier cannot be deleted
- **GIVEN** the system date is 2027-04-15 and ticket `T001` is dated 2027-04-15
- **WHEN** `TICKET DELETE;T001` is processed
- **THEN** an error message is displayed and `T001` still exists

#### Scenario: Unknown ticket
- **WHEN** `TICKET DELETE;T999` is processed and no ticket `T999` exists
- **THEN** an error message is displayed

#### Scenario: Composed ticket cannot be deleted directly
- **GIVEN** individual ticket `TG01:12345678Z` is part of future group ticket `TG01`
- **WHEN** `TICKET DELETE;TG01:12345678Z` is processed
- **THEN** an error message is displayed and the ticket and its group ticket remain

### Requirement: User deletion respects active tickets and locked groups
Deleting a user SHALL be rejected while any individual ticket associated with that user is active (dated on or after the current system date), including individual tickets that compose a group ticket, or while the user belongs to a group locked by an active group ticket. Historical tickets alone SHALL NOT prevent deletion. This blocked state SHALL be derived from the tickets and the current date, not stored. When deletion is allowed, the user SHALL be removed from every group they belong to, and their historical tickets SHALL be kept.

#### Scenario: Active ticket blocks user deletion
- **GIVEN** `ana@correo.com` has a ticket dated today or later
- **WHEN** deleting `ana@correo.com` is requested
- **THEN** the deletion is rejected with an error message

#### Scenario: Historical tickets do not block
- **GIVEN** every ticket of `ana@correo.com` is dated before the current system date
- **WHEN** deleting `ana@correo.com` is requested
- **THEN** the deletion is allowed and the historical tickets remain in the system

#### Scenario: Member of a locked group
- **GIVEN** `ana@correo.com` belongs to a group with an active group ticket
- **WHEN** deleting `ana@correo.com` is requested
- **THEN** the deletion is rejected with an error message

#### Scenario: Allowed deletion leaves groups consistent
- **GIVEN** `ana@correo.com` has no active tickets and belongs to modifiable group `G01`
- **WHEN** deleting `ana@correo.com` is allowed
- **THEN** `ana@correo.com` is no longer a member of `G01`

#### Scenario: Active composed ticket blocks deletion after leaving the group
- **GIVEN** `ana@correo.com` holds composed ticket `TG01:12345678Z` of an active group ticket but is no longer a member of any group
- **WHEN** deleting `ana@correo.com` is requested
- **THEN** the deletion is rejected with an error message
