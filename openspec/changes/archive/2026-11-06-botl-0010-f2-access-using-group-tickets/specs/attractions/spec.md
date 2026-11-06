# Spec Delta

## ADDED Requirements

### Requirement: Enter an attraction with a group ticket
The system SHALL, for `ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier` with a group ticket, admit together every user associated with the individual tickets of its composition. It SHALL do so only when the group ticket's date equals the current system date, none of those users is already inside, and the attraction's free places are at least the number of those users. Entry SHALL be indivisible: if any condition fails, no user enters and an error message is displayed.

#### Scenario: Whole group enters
- **GIVEN** system date 2027-04-15, group ticket `TG01` dated 2027-04-15 composed for `ana@correo.com` and `pedro@correo.com`, and attraction `A01` with 20 free places
- **WHEN** `ATTRACTION ENTER;A01;TG01` is processed
- **THEN** a confirmation listing both users is displayed
- **AND** both are occupants of `A01`

#### Scenario: Insufficient capacity for the whole group
- **GIVEN** attraction `A02` has 1 free place and group ticket `TG01` admits 2 users
- **WHEN** `ATTRACTION ENTER;A02;TG01` is processed
- **THEN** an error message is displayed
- **AND** no member of `TG01` is inside `A02`

#### Scenario: Group ticket for another day
- **GIVEN** group ticket `TG02` is dated after the current system date
- **WHEN** `ATTRACTION ENTER;A01;TG02` is processed
- **THEN** an error message is displayed and no user enters

### Requirement: Leave an attraction
The system SHALL, for `ATTRACTION EXIT;AttractionIdentifier;Email`, remove that user from the attraction's occupants and release their place when the attraction exists and the user is inside it. Otherwise it SHALL display an error message and leave the occupants unchanged. Users who entered with a group ticket SHALL each leave individually with their own email.

#### Scenario: User leaves and frees a place
- **GIVEN** `ana@correo.com` is inside `A01`
- **WHEN** `ATTRACTION EXIT;A01;ana@correo.com` is processed
- **THEN** a confirmation is displayed
- **AND** `ana@correo.com` is no longer inside `A01` and one more place is available

#### Scenario: Group members leave one by one
- **GIVEN** `ana@correo.com` and `pedro@correo.com` entered `A01` with group ticket `TG01`
- **WHEN** `ATTRACTION EXIT;A01;ana@correo.com` is processed
- **THEN** only `pedro@correo.com` remains inside `A01`

#### Scenario: User not inside or unknown attraction
- **WHEN** `ATTRACTION EXIT` names a user who is not inside the attraction, or an unknown attraction
- **THEN** an error message is displayed and no occupant changes

### Requirement: Attraction status
The system SHALL, for `ATTRACTION STATUS;AttractionIdentifier`, display the attraction identifier, name, maximum capacity, number of users currently inside, number of available places and the users currently inside. The values SHALL be taken from the attraction's own state and SHALL NOT depend on whether occupants entered with individual or group tickets. The operation SHALL NOT change the attraction. An unknown identifier SHALL produce an error message.

#### Scenario: Status after entries and an exit
- **GIVEN** attraction `A01` (Dragon Rojo, capacity 20) that `ana@correo.com` entered with an individual ticket and `pedro@correo.com` and `luis@correo.com` entered with a group ticket, after which `luis@correo.com` left
- **WHEN** `ATTRACTION STATUS;A01` is processed
- **THEN** the output shows `A01`, `Dragon Rojo`, capacity 20, 2 users inside, 18 available places, and the emails `ana@correo.com` and `pedro@correo.com`

#### Scenario: Status of an empty attraction
- **GIVEN** attraction `A02` with capacity 12 and no occupants
- **WHEN** `ATTRACTION STATUS;A02` is processed
- **THEN** the output shows 0 users inside and 12 available places

### Requirement: ATTRACTION EXIT and STATUS listed in HELP
The system SHALL include `ATTRACTION EXIT;<AttractionIdentifier>;<Email>` and `ATTRACTION STATUS;<AttractionIdentifier>` in the output of `HELP`.

#### Scenario: HELP shows ATTRACTION EXIT and STATUS
- **WHEN** `HELP` is processed
- **THEN** the output contains both command syntaxes
