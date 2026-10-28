# tickets Specification

## Purpose
Defines tickets: the common ticket abstraction and shared identifier space, individual ticket creation and deletion, and how active tickets restrict user deletion.

## Requirements

### Requirement: Common ticket data
Every ticket SHALL have a unique identifier, a date of use and a price, and all ticket types SHALL share one identifier space: no two tickets of any type may have the same identifier at the same time.

#### Scenario: Identifier shared across ticket types
- **WHEN** a ticket `T001` exists and any ticket creation with identifier `T001` is attempted
- **THEN** the creation is rejected with an error message

### Requirement: Active and historical tickets
A ticket SHALL be active when its date is equal to or later than the current system date, and historical otherwise.

#### Scenario: Ticket becomes historical
- **WHEN** a ticket dated 2027-04-15 exists and the system date is set to 2027-04-16
- **THEN** the ticket is historical

### Requirement: TICKET CREATE command
The system SHALL accept `TICKET CREATE;<identifier>;<email>;<date>` and create an individual ticket for the existing user with that e-mail, valid only on `<date>`, priced at the current base price, and display that price. It SHALL reject with an error message a duplicate identifier, an unknown user, a date earlier than the current system date, an invalid date, or a wrong number of fields.

#### Scenario: Create a ticket at the default price
- **WHEN** the system date is 2027-04-01, user `ana@correo.com` exists, SYSTEM PRICE has not been used and `TICKET CREATE;T001;ana@correo.com;2027-04-15` is processed
- **THEN** ticket `T001` for that user dated 2027-04-15 is created and the output shows the price 40.00

#### Scenario: Ticket for the current date allowed
- **WHEN** the system date is 2027-04-15 and `TICKET CREATE;T002;ana@correo.com;2027-04-15` is processed
- **THEN** the ticket is created

#### Scenario: Price fixed at creation
- **WHEN** `SYSTEM PRICE;45.50` is processed, ticket `T003` is created, and then `SYSTEM PRICE;50` is processed
- **THEN** ticket `T003` keeps the price 45.50

#### Scenario: Past date rejected
- **WHEN** the system date is 2027-04-15 and `TICKET CREATE;T004;ana@correo.com;2027-04-14` is processed
- **THEN** an error message is displayed and no ticket is created

#### Scenario: Unknown user rejected
- **WHEN** `TICKET CREATE;T005;nadie@correo.com;2027-04-15` is processed and no such user exists
- **THEN** an error message is displayed and no ticket is created

### Requirement: Ticket-user relationship
An individual ticket SHALL reference the user for whom it was purchased; users SHALL NOT hold references to their tickets, and a user's tickets SHALL be determined by traversing the tickets managed by the system.

#### Scenario: Tickets of a user found by traversal
- **WHEN** tickets `T001` and `T002` are created for `ana@correo.com`
- **THEN** the system finds exactly those two tickets when looking up the tickets of `ana@correo.com`

### Requirement: TICKET DELETE command
The system SHALL accept `TICKET DELETE;<identifier>` and delete the individual ticket, together with all SpeedPasses and the Special Pass associated with it, only when its date is strictly later than the current system date. Tickets for the current day or earlier SHALL be retained and the deletion rejected with an error message, as SHALL deletion of an unknown identifier or of an individual ticket belonging to a group ticket's composition.

#### Scenario: Delete a future ticket
- **WHEN** the system date is 2027-04-01, ticket `T001` is dated 2027-04-15 and `TICKET DELETE;T001` is processed
- **THEN** ticket `T001` no longer exists

#### Scenario: Current-day ticket cannot be deleted
- **WHEN** the system date is 2027-04-15, ticket `T001` is dated 2027-04-15 and `TICKET DELETE;T001` is processed
- **THEN** an error message is displayed and ticket `T001` still exists

#### Scenario: Unknown ticket
- **WHEN** `TICKET DELETE;NOPE` is processed and no such ticket exists
- **THEN** an error message is displayed

#### Scenario: Passes deleted with their ticket
- **WHEN** future ticket `T001` has SpeedPasses from two purchases and a Special Pass, and `TICKET DELETE;T001` is processed
- **THEN** the ticket and all those passes no longer exist

### Requirement: Active tickets block user deletion
Deleting a user SHALL be rejected while any active ticket (an individual ticket or an individual ticket in a group ticket's composition) references that user, and while the user belongs to a group that is not modifiable. The locked state SHALL be derived from the existing tickets and the current system date, not stored. When deletion is permitted, the user SHALL be removed from all groups they belong to, and historical tickets SHALL be retained.

#### Scenario: Active ticket blocks deletion
- **WHEN** user `ana@correo.com` has a ticket dated on or after the current system date and deletion of that user is requested
- **THEN** the deletion is rejected and the user still exists

#### Scenario: Historical ticket does not block deletion
- **WHEN** user `ana@correo.com` only has tickets dated before the current system date and deletion of that user is requested
- **THEN** the user is deleted, removed from their groups, and the historical tickets still exist and reference the user data
