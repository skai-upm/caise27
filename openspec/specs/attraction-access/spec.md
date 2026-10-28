# attraction-access Specification

## Purpose
Defines how users enter park attractions with a ticket valid for the current system date, respecting attraction capacity.

## Requirements

### Requirement: ATTRACTION ENTER with an individual ticket
The system SHALL accept `ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier` and, when the attraction and an individual ticket with that identifier exist and the ticket's date equals the current system date, add the ticket's user to the attraction's occupants.

#### Scenario: User enters with a ticket for today
- **WHEN** the system date is 2027-04-15, ticket `T001` for `ana@correo.com` is dated 2027-04-15, attraction `A01` has free capacity, and `ATTRACTION ENTER;A01;T001` is processed
- **THEN** `ana@correo.com` is listed by `ATTRACTION LIST;A01`

### Requirement: Ticket validity checked at entry
Entry SHALL be rejected with an error message when the ticket does not exist or its date differs from the current system date. The date SHALL be checked only on entry: later changes of the system date SHALL NOT remove users already inside.

#### Scenario: Ticket for another date
- **WHEN** the system date is 2027-04-14, ticket `T001` is dated 2027-04-15 and `ATTRACTION ENTER;A01;T001` is processed
- **THEN** an error message is displayed and nobody enters `A01`

#### Scenario: Unknown ticket or attraction
- **WHEN** `ATTRACTION ENTER;A01;NOPE` or `ATTRACTION ENTER;A99;T001` is processed
- **THEN** an error message is displayed and no attraction changes

#### Scenario: Presence survives date change
- **WHEN** `ana@correo.com` entered `A01` with a ticket for today and then `SYSTEM DATE` moves to the next day
- **THEN** `ana@correo.com` is still listed in `A01`

### Requirement: Capacity on entry
Entry SHALL be rejected with an error message when the attraction lacks capacity for every user the ticket admits; in that case nobody enters. A user already inside the attraction SHALL NOT enter it again.

#### Scenario: Attraction full
- **WHEN** attraction `A01` has capacity 1 and already holds one user, and another user's valid ticket is used with `ATTRACTION ENTER;A01;T002`
- **THEN** an error message is displayed and `A01` still holds exactly one user

#### Scenario: Already inside
- **WHEN** `ana@correo.com` is inside `A01` and `ATTRACTION ENTER;A01;T001` is processed again
- **THEN** an error message is displayed and she appears once in `A01`

### Requirement: Common access mechanism
Attraction access SHALL be implemented through the common ticket abstraction, admitting the users of the ticket's individual tickets, with no separate access mechanism per ticket type.

#### Scenario: Access through the abstraction
- **WHEN** the access operation receives any ticket
- **THEN** the users admitted are those referenced by the individual tickets the ticket consists of
