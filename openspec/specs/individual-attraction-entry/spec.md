# individual-attraction-entry Specification

## Purpose

Admits ticket holders into attractions using a ticket identifier, through one access operation based on the common ticket abstraction.

## Requirements

### Requirement: Ticket-based entry
`ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier` SHALL admit the users of an existing ticket whose date equals the current system date into an existing attraction when there is capacity for all of them; for an individual ticket its user enters. The date SHALL be checked only at entry; later date changes SHALL NOT remove occupants. (F2-C06)

#### Scenario: Valid individual entry
- **WHEN** T001 is Ana's ticket for today and A01 has a free place
- **THEN** `ATTRACTION ENTER;A01;T001` adds Ana to A01's occupants

#### Scenario: Wrong date
- **WHEN** the ticket is dated another day
- **THEN** entry is rejected and occupancy is unchanged

#### Scenario: Full attraction
- **WHEN** A01 is at capacity
- **THEN** entry is rejected and occupancy is unchanged

#### Scenario: Presence independent of later date
- **WHEN** Ana entered with today's ticket and `SYSTEM DATE` then moves forward
- **THEN** Ana is still inside A01

#### Scenario: Unknown ticket or attraction, or already inside
- **WHEN** the ticket or the attraction does not exist, or a user of the ticket is already inside that attraction
- **THEN** entry is rejected and occupancy is unchanged
