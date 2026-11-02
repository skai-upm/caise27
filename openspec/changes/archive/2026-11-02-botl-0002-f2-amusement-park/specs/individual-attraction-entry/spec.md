# Spec Delta

## Purpose

Admits users into an attraction with an existing ticket valid for the current system date, while capacity allows.

## ADDED Requirements

### Requirement: ATTRACTION ENTER with an individual ticket
`ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier` SHALL admit the user of an existing individual ticket into an existing attraction when the ticket date equals the current system date and a place is available, adding the user to the attraction's occupants.

#### Scenario: Valid entry
- **WHEN** T001 is Ana's ticket for today, A01 has free places and `ATTRACTION ENTER;A01;T001` is processed
- **THEN** Ana is among A01's occupants

#### Scenario: Ticket for another date
- **WHEN** the ticket is dated on a different day than the current system date
- **THEN** entry is rejected and occupancy is unchanged

#### Scenario: Full attraction
- **WHEN** A01 is at capacity
- **THEN** entry is rejected and occupancy is unchanged

#### Scenario: Unknown ticket or attraction
- **WHEN** the ticket or attraction does not exist
- **THEN** entry is rejected and an error is reported

### Requirement: Validity checked only at entry
The ticket date SHALL be checked only when entering; a user's presence inside an attraction SHALL NOT depend on later changes to the system date.

#### Scenario: Date changes after entry
- **WHEN** Ana entered A01 with a ticket for today and `SYSTEM DATE` is then moved to another day
- **THEN** Ana remains among A01's occupants

### Requirement: Common entry mechanism
Entry SHALL use the common ticket abstraction to obtain the users to admit, without separate access mechanisms per ticket type.

#### Scenario: Same command for both ticket types
- **WHEN** `ATTRACTION ENTER` is given an individual ticket identifier or a group ticket identifier
- **THEN** the same command admits the ticket's users
