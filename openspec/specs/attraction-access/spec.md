# attraction-access Specification

## Purpose

Defines how users get into Amusement Park attractions by presenting a ticket: which tickets are valid, which users are admitted and how the attraction's capacity limits entry.

## Requirements

### Requirement: Enter an attraction with an individual ticket
`ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier` with an existing individual ticket SHALL admit the ticket's user into the existing attraction when the ticket is valid for the current system date and the attraction has free capacity, and print a confirmation naming the user's email.

#### Scenario: Valid ticket and free capacity
- **GIVEN** the system date is `2027-04-15`, attraction `A01` has capacity 20 and 0 users inside, and Ana's ticket `T001` is dated `2027-04-15`
- **WHEN** `ATTRACTION ENTER;A01;T001` is processed
- **THEN** a confirmation naming `ana@correo.com` is printed and `ATTRACTION LIST;A01` shows `ana@correo.com`

#### Scenario: Unknown attraction or ticket
- **WHEN** `ATTRACTION ENTER;A99;T001` or `ATTRACTION ENTER;A01;T999` is processed
- **THEN** an error naming the missing attraction or ticket is printed and nobody enters

### Requirement: Ticket valid only on its date
A ticket SHALL be valid for entry only when its date equals the current system date; the date MUST be checked only at the moment of entry, and a ticket for another date MUST be rejected with an error and nobody enters.

#### Scenario: Ticket for a different date
- **GIVEN** the system date is `2027-04-15` and ticket `T002` is dated `2027-04-16` (or `2027-04-14`)
- **WHEN** `ATTRACTION ENTER;A01;T002` is processed
- **THEN** an error reporting that the ticket is not valid on `2027-04-15` is printed and nobody enters

#### Scenario: Presence independent of later date changes
- **GIVEN** Ana entered `A01` with a ticket valid on `2027-04-15`
- **WHEN** `SYSTEM DATE;2027-04-20` is processed
- **THEN** `ATTRACTION LIST;A01` still shows `ana@correo.com`

### Requirement: Capacity limits entry
The attraction MUST have free capacity for every user who needs to enter; otherwise entry SHALL be rejected with an error and nobody enters. A user already inside the attraction MUST NOT enter it again.

#### Scenario: Full attraction
- **GIVEN** attraction `A03` with capacity 1 and one user inside
- **WHEN** `ATTRACTION ENTER;A03;T001` is processed with Ana's valid ticket
- **THEN** an error reporting insufficient capacity is printed and Ana is not inside

#### Scenario: Already inside
- **GIVEN** Ana is inside `A01`
- **WHEN** she presents another valid ticket for `A01`
- **THEN** an error reporting that she is already inside is printed and she appears once

### Requirement: Access through the common ticket abstraction
Access SHALL use the behavior common to all ticket types — the users a ticket admits and its validity date — rather than a mechanism specific to one ticket type.

#### Scenario: Individual ticket admits its user
- **WHEN** the users admitted by an individual ticket are requested through the common ticket abstraction
- **THEN** exactly the ticket's user is returned
