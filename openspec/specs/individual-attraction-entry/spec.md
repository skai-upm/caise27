# individual-attraction-entry Specification

## Purpose

Lets the holder of an individual ticket valid for the current system date enter an attraction with free capacity (source F2-C06, section 10).

## Requirements

### Requirement: Enter with an individual ticket
`ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier` with an individual ticket SHALL add the ticket's user to the attraction's occupants when the attraction and ticket exist, the ticket date equals the current system date and at least one place is free.

#### Scenario: Valid entry
- **WHEN** ticket `T001` for Ana is dated today, `A01` has free places and `ATTRACTION ENTER;A01;T001` is processed
- **THEN** Ana is inside `A01` and success is reported

### Requirement: Entry rejections leave occupancy unchanged
Entry SHALL be rejected without changing occupancy or passes when the attraction or ticket does not exist, the ticket date differs from the current system date, the attraction is full, a user who would enter is already inside that attraction, the attraction type does not accept the ticket kind, or a required SpeedPass or Special Pass is not available.

#### Scenario: Ticket for another date
- **WHEN** ticket `T001` is dated tomorrow and `ATTRACTION ENTER;A01;T001` is processed
- **THEN** an error is reported and `A01` occupancy is unchanged

#### Scenario: Full attraction
- **WHEN** `A01` is at capacity and a valid ticket for today is used to enter
- **THEN** an error is reported and occupancy is unchanged

#### Scenario: Ticket kind not accepted
- **WHEN** a valid individual ticket is used at a GROUP attraction
- **THEN** an error is reported and occupancy is unchanged

### Requirement: Presence independent of later date changes
The ticket date SHALL be checked only at entry; a user already inside SHALL remain inside when the system date changes afterwards.

#### Scenario: Date change after entry
- **WHEN** Ana entered `A01` today and `SYSTEM DATE` moves to tomorrow
- **THEN** Ana is still listed inside `A01`
