# Spec Delta

## Purpose

Defines how users access attractions with tickets: the ticket must exist and be valid for the current system date, the attraction must have enough capacity, and the users of the ticket become occupants of the attraction.

## ADDED Requirements

### Requirement: ATTRACTION ENTER with an individual ticket
The command `ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier` with an existing individual ticket dated on the current system date SHALL add the ticket's user to the attraction's occupants and print `USER ENTERED ATTRACTION: <email>`.

#### Scenario: Valid entry
- **WHEN** the current date is 2027-04-15, Ana has ticket `T001` dated 2027-04-15, attraction `A01` has room, and `ATTRACTION ENTER;A01;T001` is processed
- **THEN** `USER ENTERED ATTRACTION: ana@correo.com` is printed and Ana is listed by `ATTRACTION LIST;A01`

### Requirement: Ticket valid for the current date
Entry SHALL be rejected with an `ERROR:` message if the attraction does not exist, the ticket does not exist, or the ticket's date is not the current system date. The ticket date SHALL be checked only at entry: a user already inside SHALL remain inside when the system date changes afterwards.

#### Scenario: Ticket for another day
- **WHEN** the current date is 2027-04-15 and `ATTRACTION ENTER;A01;T002` is processed for a ticket dated 2027-04-16
- **THEN** an `ERROR:` line about the ticket date is printed and nobody enters

#### Scenario: Presence survives a date change
- **WHEN** Ana entered `A01` with a ticket for 2027-04-15 and then `SYSTEM DATE;2027-04-16` is processed
- **THEN** Ana is still listed by `ATTRACTION LIST;A01`

#### Scenario: Unknown ticket or attraction
- **WHEN** `ATTRACTION ENTER;A01;T999` or `ATTRACTION ENTER;A99;T001` is processed and that ticket or attraction does not exist
- **THEN** an `ERROR:` line is printed and nobody enters

### Requirement: Capacity limit
Entry SHALL be rejected with an `ERROR:` message when the attraction does not have room for every user entering with the ticket; in that case nobody SHALL enter.

#### Scenario: Full attraction
- **WHEN** attraction `A03` has capacity 1 and already contains one user, and another user with a valid ticket tries to enter
- **THEN** an `ERROR:` line about capacity is printed and the occupants of `A03` are unchanged

### Requirement: No duplicate presence
A user already inside an attraction SHALL NOT enter it again; such an entry SHALL be rejected with an `ERROR:` message.

#### Scenario: Enter twice
- **WHEN** Ana is inside `A01` and `ATTRACTION ENTER;A01;T001` is processed again
- **THEN** an `ERROR:` line is printed and Ana appears once in `A01`

### Requirement: Single access mechanism
Access SHALL be implemented once on the common ticket abstraction: the users entering are those of the ticket's individual tickets, so every ticket type uses the same capacity and presence checks.

#### Scenario: Common mechanism
- **WHEN** the access operation is given any ticket type
- **THEN** it determines the entering users from the ticket's individual tickets and applies the same checks
