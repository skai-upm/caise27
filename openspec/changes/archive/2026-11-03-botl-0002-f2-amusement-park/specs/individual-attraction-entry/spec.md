# Spec Delta

## Purpose

Admits users into attractions with an individual ticket valid for the current system date, within capacity.

## ADDED Requirements

### Requirement: Entry with an individual ticket
`ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier` with an existing individual ticket dated on the current system date SHALL add the ticket's user to the attraction's occupants when a place is available.

#### Scenario: Valid entry
- **WHEN** a valid individual ticket for today is used on an attraction with a free place
- **THEN** its user is added to the occupants

### Requirement: Entry rejection
Entry SHALL be rejected without changing occupancy when the attraction or ticket does not exist, the ticket date differs from the current system date, the attraction is full, the holder is already inside that attraction, or the holder is no longer a registered user.

#### Scenario: Ticket for another date
- **WHEN** a ticket dated tomorrow is used
- **THEN** entry is rejected and occupancy is unchanged

#### Scenario: Full attraction
- **WHEN** an attraction is at capacity and another valid ticket is used
- **THEN** entry is rejected and occupancy is unchanged

### Requirement: Date checked only at entry
The ticket date SHALL be checked only when entering; a user's presence SHALL NOT depend on later system date changes.

#### Scenario: Date changes after entry
- **WHEN** a user has entered and the system date is then changed
- **THEN** the user remains inside the attraction
