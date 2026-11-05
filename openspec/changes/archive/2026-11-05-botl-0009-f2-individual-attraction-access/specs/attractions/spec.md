# Spec Delta

## ADDED Requirements

### Requirement: Attraction entry with an individual ticket
The command `ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier` SHALL admit the user associated with an existing individual ticket into an existing attraction. The ticket's date SHALL equal the current system date and the attraction SHALL have free capacity. Otherwise the entry SHALL be rejected with an error and occupants SHALL remain unchanged.

#### Scenario: Valid ticket and free capacity
- **GIVEN** the system date 2027-04-15, ticket `T001` for `ana@correo.com` dated 2027-04-15, and attraction `A01` with free capacity
- **WHEN** `ATTRACTION ENTER;A01;T001` is processed
- **THEN** a confirmation is shown and `ana@correo.com` is inside `A01`

#### Scenario: Ticket not valid today
- **GIVEN** ticket `T002` dated 2027-04-16 and the system date 2027-04-15
- **WHEN** `ATTRACTION ENTER;A01;T002` is processed
- **THEN** an error is shown and the occupants of `A01` are unchanged

#### Scenario: Attraction full
- **GIVEN** attraction `A02` whose occupants already equal its capacity
- **WHEN** a valid ticket is used to enter `A02`
- **THEN** an error is shown and no one enters

#### Scenario: Unknown ticket or attraction
- **WHEN** `ATTRACTION ENTER` names a ticket or attraction that does not exist
- **THEN** an error is shown and no occupants change

#### Scenario: User already inside
- **GIVEN** `ana@correo.com` is already inside `A01`
- **WHEN** another valid ticket of `ana@correo.com` is used to enter `A01`
- **THEN** an error is shown and `ana@correo.com` appears only once

### Requirement: Entry date checked only at entry
The ticket's date SHALL be checked only when entering. A user's presence in an attraction SHALL NOT depend on later changes to the system date.

#### Scenario: Date changes after entry
- **GIVEN** `ana@correo.com` entered `A01` with a ticket valid on 2027-04-15
- **WHEN** `SYSTEM DATE;2027-04-20` is processed
- **THEN** `ana@correo.com` is still inside `A01`
