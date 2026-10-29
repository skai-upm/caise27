# Spec Delta

## Purpose

Defines park tickets: the information and behavior common to every ticket type, the shared identifier space, creation and deletion of individual tickets, and how ticket-derived states of users are determined.

## ADDED Requirements

### Requirement: Common ticket information
Every ticket SHALL have a unique identifier, a date of use and price information, and ticket types SHALL share this information and their price-related behavior through a common abstraction so that operations not needing the concrete type can handle any ticket.

#### Scenario: Ticket exposes common data
- **WHEN** a ticket is handled through the common abstraction
- **THEN** its identifier, date and price are available without knowing its concrete type

### Requirement: Shared ticket identifier space
All ticket types SHALL share one identifier space: creating any ticket SHALL be rejected when its identifier, or the identifier of any ticket it contains, is already used by an existing ticket, and in that case nothing SHALL be created.

#### Scenario: Duplicate identifier rejected
- **WHEN** ticket `T001` exists and `TICKET CREATE;T001;pedro@correo.com;2027-04-16` is processed
- **THEN** an error message is shown and no new ticket is created

### Requirement: Create individual ticket
The command `TICKET CREATE;<identifier>;<email>;<date>` SHALL create an individual ticket valid only on `<date>` (format `yyyy-MM-dd`) for the existing user with that email, priced at the current base price, and SHALL display the price charged. It SHALL be rejected with an error message when the user does not exist, the date is invalid or earlier than the current system date, the identifier is blank or already used, or the number of arguments is wrong.

#### Scenario: Create ticket at the default price
- **WHEN** the current date is 2027-04-01, user `ana@correo.com` exists, SYSTEM PRICE has not been used, and `TICKET CREATE;T001;ana@correo.com;2027-04-15` is processed
- **THEN** ticket `T001` for `ana@correo.com` dated 2027-04-15 is created and the price 40.00 is displayed

#### Scenario: Price follows the current system price
- **WHEN** `SYSTEM PRICE;45.50` has been processed and a ticket is then created
- **THEN** the ticket's price is 45.50 and 45.50 is displayed

#### Scenario: Changing the price does not alter existing tickets
- **WHEN** a ticket was created at 40.00 and `SYSTEM PRICE;50` is processed afterwards
- **THEN** the existing ticket's price remains 40.00

#### Scenario: Ticket for today allowed, past date rejected
- **WHEN** the current date is 2027-04-15 and tickets are requested for 2027-04-15 and for 2027-04-14
- **THEN** the ticket for 2027-04-15 is created and the one for 2027-04-14 is rejected with an error message

#### Scenario: Unknown user rejected
- **WHEN** `TICKET CREATE;T002;nadie@correo.com;2027-04-15` is processed and no such user exists
- **THEN** an error message is shown and no ticket is created

### Requirement: Individual ticket references its user
An individual ticket SHALL hold a reference to the user for whom it was purchased, and users SHALL NOT hold references to their tickets; the tickets of a user SHALL be determined by traversing the tickets managed by the system.

#### Scenario: Tickets of a user found by traversal
- **WHEN** tickets `T001` and `T002` exist for `ana@correo.com` and `T003` for `pedro@correo.com`
- **THEN** the tickets determined for `ana@correo.com` are exactly `T001` and `T002`

### Requirement: Delete individual ticket
The command `TICKET DELETE;<identifier>` SHALL delete the individual ticket only when its date is strictly later than the current system date. Tickets dated on the current day or earlier SHALL NOT be deleted and SHALL be kept as historical records. An unknown identifier SHALL be rejected with an error message.

#### Scenario: Delete future ticket
- **WHEN** the current date is 2027-04-01 and ticket `T001` is dated 2027-04-15, and `TICKET DELETE;T001` is processed
- **THEN** a confirmation is shown and `T001` no longer exists

#### Scenario: Today's or past ticket kept
- **WHEN** the current date is 2027-04-15 and ticket `T001` is dated 2027-04-15 (or the date later advances past it), and `TICKET DELETE;T001` is processed
- **THEN** an error message is shown and `T001` still exists

#### Scenario: Unknown ticket rejected
- **WHEN** `TICKET DELETE;T999` is processed and no such ticket exists
- **THEN** an error message is shown

### Requirement: Active tickets block user deletion
A ticket SHALL be considered active when its date is equal to or later than the current system date and historical otherwise. Whether a user has an active ticket SHALL be derived from the existing tickets and the current system date, never stored on the user, and a user with an active ticket SHALL NOT be deletable.

#### Scenario: User with active ticket
- **WHEN** the current date is 2027-04-15 and `ana@correo.com` has a ticket dated 2027-04-15
- **THEN** the user is reported as having an active ticket and cannot be deleted

#### Scenario: Only historical tickets
- **WHEN** the current date moves to 2027-04-16 and `ana@correo.com` only has the ticket dated 2027-04-15
- **THEN** the user is reported as having no active ticket
