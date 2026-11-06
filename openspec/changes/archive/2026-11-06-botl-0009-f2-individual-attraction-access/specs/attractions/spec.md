# Spec Delta

## ADDED Requirements

### Requirement: Enter an attraction with an individual ticket
The system SHALL, for `ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier` with an individual ticket, add the ticket's user to the attraction's occupants when the attraction and ticket exist, the ticket date equals the current system date, the user is not already inside, and the attraction has at least one free place. A confirmation SHALL be displayed. Otherwise it SHALL reject the entry with an error message and leave the occupants unchanged.

#### Scenario: Valid ticket and free capacity
- **GIVEN** system date 2027-04-15, attraction `A01` with capacity 20 and no occupants, and ticket `T001` for `ana@correo.com` dated 2027-04-15
- **WHEN** `ATTRACTION ENTER;A01;T001` is processed
- **THEN** a confirmation is displayed
- **AND** `ana@correo.com` is an occupant of `A01`

#### Scenario: Ticket not valid today
- **GIVEN** system date 2027-04-15 and ticket `T002` dated 2027-04-16
- **WHEN** `ATTRACTION ENTER;A01;T002` is processed
- **THEN** an error message is displayed and the occupants of `A01` are unchanged

#### Scenario: Attraction full
- **GIVEN** attraction `A03` with capacity 1 that already has one occupant, and a valid ticket for another user
- **WHEN** that user tries to enter `A03`
- **THEN** an error message is displayed and the occupants of `A03` are unchanged

#### Scenario: Unknown attraction or ticket, or user already inside
- **WHEN** `ATTRACTION ENTER` names an unknown attraction or ticket, or the ticket's user is already inside the attraction
- **THEN** an error message is displayed and no occupant is added

### Requirement: Presence does not depend on later date changes
The system SHALL check the ticket date only at the moment of entry. Users already inside an attraction SHALL remain inside when the system date changes afterwards.

#### Scenario: Date changes after entry
- **GIVEN** `ana@correo.com` entered `A01` with a ticket valid on 2027-04-15
- **WHEN** `SYSTEM DATE;2027-04-16` is processed
- **THEN** `ATTRACTION LIST;A01` still shows `ana@correo.com`

### Requirement: Single access mechanism for all ticket types
The system SHALL perform attraction entry through one operation that works on any ticket: it obtains from the ticket the users it admits, then applies the same validity and capacity checks to all of them at once, without separate access logic per ticket type.

#### Scenario: Individual ticket admits its user
- **GIVEN** a valid individual ticket for `ana@correo.com`
- **WHEN** it is used to enter an attraction
- **THEN** exactly `ana@correo.com` is admitted

### Requirement: ATTRACTION ENTER listed in HELP
The system SHALL include `ATTRACTION ENTER;<AttractionIdentifier>;<TicketIdentifier>` in the output of `HELP`.

#### Scenario: HELP shows ATTRACTION ENTER
- **WHEN** `HELP` is processed
- **THEN** the output contains `ATTRACTION ENTER;<AttractionIdentifier>;<TicketIdentifier>`
