# Spec Delta

## Purpose

Defines park tickets: the information and behavior common to every ticket type, the shared identifier space, individual ticket purchase and deletion, and the effect of active tickets on user deletion.

## ADDED Requirements

### Requirement: Common ticket information and identifier space
Every ticket SHALL have a unique identifier, a date of use and a price. All ticket types SHALL share one identifier space: creating any ticket whose identifier is already used by a ticket of any type SHALL be rejected, leaving existing tickets unchanged.

#### Scenario: Identifier reused
- **GIVEN** ticket `T001` exists
- **WHEN** `TICKET CREATE;T001;ana@correo.com;2027-04-20` is processed
- **THEN** an error is shown and the existing `T001` is unchanged

### Requirement: Individual ticket creation
The command `TICKET CREATE;<identifier>;<email>;<date>` SHALL create a ticket for the existing user with that e-mail, valid only on `<date>` (format `yyyy-MM-dd`), priced at the current base price, and SHALL display that price. It SHALL be rejected with an error and no ticket created when the identifier is empty or already used, the user does not exist, or the date is invalid or earlier than the current system date.

#### Scenario: Ticket created at the initial price
- **GIVEN** the system date is 2027-04-15, `SYSTEM PRICE` has not been used and `ana@correo.com` is registered
- **WHEN** `TICKET CREATE;T001;ana@correo.com;2027-04-15` is processed
- **THEN** ticket `T001` for that user and date exists with price 40.00 and the output shows 40.00

#### Scenario: Ticket uses the configured price
- **GIVEN** `SYSTEM PRICE;45.50` has been processed
- **WHEN** a valid `TICKET CREATE` is processed
- **THEN** the ticket price is 45.50

#### Scenario: Past date rejected
- **GIVEN** the system date is 2027-04-15
- **WHEN** `TICKET CREATE;T002;ana@correo.com;2027-04-14` is processed
- **THEN** an error is shown and no ticket `T002` exists

#### Scenario: Unknown user rejected
- **WHEN** `TICKET CREATE;T003;nobody@correo.com;2027-04-20` is processed
- **THEN** an error is shown and no ticket `T003` exists

#### Scenario: Later price change does not affect existing tickets
- **GIVEN** ticket `T001` was created at 40.00
- **WHEN** `SYSTEM PRICE;50` is processed
- **THEN** `T001` still costs 40.00

### Requirement: Individual ticket deletion
The command `TICKET DELETE;<identifier>` SHALL delete an individual ticket only when its date is strictly later than the current system date. Tickets dated today or earlier SHALL be kept as historical records and the deletion rejected; unknown identifiers SHALL be rejected.

#### Scenario: Future ticket deleted
- **GIVEN** the system date is 2027-04-15 and ticket `T001` is dated 2027-04-20
- **WHEN** `TICKET DELETE;T001` is processed
- **THEN** a confirmation is shown and `T001` no longer exists

#### Scenario: Today's or past ticket kept
- **GIVEN** ticket `T001` dated 2027-04-15 and the system date 2027-04-15 (or later)
- **WHEN** `TICKET DELETE;T001` is processed
- **THEN** an error is shown and `T001` still exists

#### Scenario: Unknown ticket
- **WHEN** `TICKET DELETE;T999` is processed and no such ticket exists
- **THEN** an error is shown

### Requirement: Active tickets block user deletion
A ticket SHALL be active when its date is equal to or later than the current system date. Deleting a user SHALL be rejected while any active ticket is associated with that user; historical tickets alone SHALL NOT block it. This locked state SHALL be derived from the tickets managed by the system and never stored on the user.

#### Scenario: Active ticket blocks deletion
- **GIVEN** `ana@correo.com` has a ticket dated on the current system date
- **WHEN** deletion of that user is requested
- **THEN** the deletion is rejected

#### Scenario: Only historical tickets
- **GIVEN** all tickets of `ana@correo.com` are dated before the current system date
- **WHEN** deletion of that user is requested
- **THEN** the ticket rule does not block the deletion and the tickets are retained
