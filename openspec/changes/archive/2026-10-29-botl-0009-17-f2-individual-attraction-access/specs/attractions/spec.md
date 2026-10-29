# Spec Delta

## ADDED Requirements

### Requirement: Enter attraction with an individual ticket
The command `ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier` SHALL make the user of the given individual ticket enter the attraction when the attraction exists, the ticket exists, the ticket's date equals the current system date, the attraction has room for the user, and the user is not already inside it. Otherwise it SHALL be rejected with an error message and no user SHALL enter. The ticket date SHALL be checked only at entry.

#### Scenario: Enter with today's ticket
- **WHEN** the current date is 2027-04-15, `T001` is an individual ticket of `ana@correo.com` dated 2027-04-15, and `ATTRACTION ENTER;A01;T001` is processed
- **THEN** a confirmation is shown and `ATTRACTION LIST;A01` includes `ana@correo.com`

#### Scenario: Ticket for another date rejected
- **WHEN** the current date is 2027-04-14 (or 2027-04-16) and `ATTRACTION ENTER;A01;T001` is processed for a ticket dated 2027-04-15
- **THEN** an error message is shown and nobody enters

#### Scenario: Full attraction rejected
- **WHEN** attraction `A03` has capacity 1 and already holds one user, and another valid ticket is used to enter it
- **THEN** an error message is shown and the attraction still holds one user

#### Scenario: Unknown attraction or ticket rejected
- **WHEN** `ATTRACTION ENTER;A99;T001` or `ATTRACTION ENTER;A01;T999` is processed and the attraction or ticket does not exist
- **THEN** an error message is shown

#### Scenario: Presence independent of later date changes
- **WHEN** `ana@correo.com` entered `A01` on 2027-04-15 and `SYSTEM DATE;2027-04-16` is then processed
- **THEN** `ana@correo.com` is still inside `A01`

### Requirement: Access through the common ticket abstraction
Attraction access SHALL be decided through the abstraction common to all ticket types, each ticket type providing the users it admits, rather than through separate access mechanisms per ticket type; the attraction SHALL have room for every admitted user or none SHALL enter.

#### Scenario: Users admitted by an individual ticket
- **WHEN** access is requested with an individual ticket
- **THEN** exactly the ticket's user is admitted
