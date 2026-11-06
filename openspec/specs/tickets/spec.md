# tickets Specification

## Purpose
Defines park tickets: a common ticket abstraction with a shared identifier space, individual tickets that users buy for a specific date at the current base price, their deletion rules, and how a user's active tickets restrict deleting that user.

## Requirements

### Requirement: Common ticket information and identifier space
Every ticket SHALL have a unique identifier, a date of use and a price. Ticket identifiers SHALL form a single space shared by all ticket types, so no two tickets of any type can have the same identifier at the same time.

#### Scenario: Identifier already used by another ticket
- **GIVEN** a ticket `T001` exists
- **WHEN** a ticket of any type is created with identifier `T001`
- **THEN** the creation is rejected with an error message
- **AND** the existing ticket is unchanged

### Requirement: Create an individual ticket
The system SHALL create an individual ticket with `TICKET CREATE;<identifier>;<email>;<date>` when the identifier is unused, a user with that email exists, and the date (yyyy-MM-dd) is equal to or later than the current system date. The ticket SHALL be associated with that user, be valid only on its date, and cost the current base price at the moment of creation. The confirmation SHALL display the price charged.

#### Scenario: Ticket at the default price
- **GIVEN** user `ana@correo.com` exists, the system date is 2027-04-15 and SYSTEM PRICE has not been used
- **WHEN** `TICKET CREATE;T001;ana@correo.com;2027-04-15` is processed
- **THEN** ticket `T001` is created for `ana@correo.com` dated 2027-04-15
- **AND** the confirmation displays the price 40.00

#### Scenario: Ticket at a configured price
- **GIVEN** `SYSTEM PRICE;45.50` was processed
- **WHEN** a valid `TICKET CREATE` is processed
- **THEN** the ticket price is 45.50 and it is displayed

#### Scenario: Price change does not affect existing tickets
- **GIVEN** ticket `T001` was created at 40.00
- **WHEN** `SYSTEM PRICE;50.00` is processed
- **THEN** ticket `T001` still costs 40.00

#### Scenario: Invalid creation is rejected
- **WHEN** `TICKET CREATE` is processed with a date earlier than the system date, an invalid date, an email of no existing user, a duplicate identifier, or missing fields
- **THEN** an error message is displayed and no ticket is created

### Requirement: Delete an individual ticket
The system SHALL delete an individual ticket with `TICKET DELETE;<identifier>` only when it exists and its date is strictly later than the current system date. Tickets dated on or before the current system date SHALL be kept as historical records and the deletion rejected with an error message.

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

### Requirement: Users do not store their tickets
The association between an individual ticket and its user SHALL be one-way: the ticket identifies its user, and a user's tickets SHALL be determined by traversing the tickets managed by the system.

#### Scenario: Tickets of a user
- **GIVEN** tickets `T001` and `T002` belong to `ana@correo.com` and `T003` to `pedro@correo.com`
- **WHEN** the tickets of `ana@correo.com` are requested
- **THEN** exactly `T001` and `T002` are obtained from the system's tickets

### Requirement: User deletion respects active tickets and locked groups
Deleting a user SHALL be rejected while any individual ticket associated with that user is active (dated on or after the current system date) or while the user belongs to a group locked by an active group ticket. Historical tickets alone SHALL NOT prevent deletion. This blocked state SHALL be derived from the tickets and the current date, not stored. When deletion is allowed, the user SHALL be removed from every group they belong to, and their historical tickets SHALL be kept.

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

### Requirement: TICKET commands listed in HELP
The system SHALL include `TICKET CREATE;<identifier>;<email>;<date>` and `TICKET DELETE;<identifier>` in the output of `HELP`.

#### Scenario: HELP shows TICKET commands
- **WHEN** `HELP` is processed
- **THEN** the output contains both TICKET command syntaxes
