## MODIFIED Requirements

### Requirement: Entry with an individual ticket
`ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier` SHALL, for an existing attraction and an existing individual ticket whose date equals the current system date, add the ticket's user to the attraction's occupants when a place is available. The date SHALL be checked only at entry; later date changes SHALL NOT affect users already inside. From Delivery 3 this applies only at attractions whose type accepts individual tickets (GENERAL, INDIVIDUAL and, with an unconsumed Special Pass, SPECIAL).

#### Scenario: Valid entry
- **WHEN** Ana has ticket T001 for the current date and A01 has a free place and `ATTRACTION ENTER;A01;T001` is processed
- **THEN** Ana is inside A01

#### Scenario: Presence independent of later date changes
- **WHEN** Ana has entered A01 and `SYSTEM DATE` is changed to another day
- **THEN** Ana is still inside A01

#### Scenario: Individual ticket at a GROUP attraction
- **WHEN** a valid individual ticket is used at a GROUP attraction
- **THEN** entry is rejected and occupancy is unchanged
