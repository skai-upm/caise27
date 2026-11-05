# Spec Delta

## ADDED Requirements

### Requirement: Attraction entry with a group ticket
`ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier` with a group ticket valid on the current system date SHALL admit together all the users of the tickets that make up the group ticket, using the same access operation as individual tickets. Group access SHALL be indivisible: if free capacity cannot hold all of them, or any of them is already inside, none SHALL enter.

#### Scenario: Whole group fits
- **GIVEN** group ticket `TG01` dated today for 3 members, and attraction `A01` with 3 or more free places
- **WHEN** `ATTRACTION ENTER;A01;TG01` is processed
- **THEN** all 3 members are inside `A01`

#### Scenario: Not enough capacity for the group
- **GIVEN** group ticket `TG01` dated today for 3 members, and attraction `A02` with 2 free places
- **WHEN** `ATTRACTION ENTER;A02;TG01` is processed
- **THEN** an error is shown and no member enters `A02`

#### Scenario: Group ticket not valid today
- **GIVEN** group ticket `TG02` dated after the current system date
- **WHEN** `ATTRACTION ENTER;A01;TG02` is processed
- **THEN** an error is shown and no one enters

### Requirement: Leaving an attraction
The command `ATTRACTION EXIT;AttractionIdentifier;Email` SHALL remove that user from the attraction's occupants and free their place. Users who entered with a group ticket SHALL leave individually with their own e-mail. An unknown attraction or a user who is not inside SHALL be reported as an error, leaving occupants unchanged.

#### Scenario: User leaves
- **GIVEN** `ana@correo.com` is inside `A01`
- **WHEN** `ATTRACTION EXIT;A01;ana@correo.com` is processed
- **THEN** `ana@correo.com` is no longer inside and one more place is available

#### Scenario: Group members leave one by one
- **GIVEN** the members of `TG01` entered `A01` together
- **WHEN** one member processes `ATTRACTION EXIT` with their e-mail
- **THEN** only that member leaves and the others remain inside

#### Scenario: User not inside
- **WHEN** `ATTRACTION EXIT;A01;luis@correo.com` is processed and that user is not inside `A01`
- **THEN** an error is shown and occupants are unchanged

### Requirement: Attraction status
The command `ATTRACTION STATUS;AttractionIdentifier` SHALL display the attraction identifier, name, maximum capacity, number of users currently inside, number of available places and the users currently inside. These SHALL be obtained from the attraction's own state, treating every occupant the same whatever ticket they entered with. Unknown attractions SHALL be reported as an error.

#### Scenario: Status after mixed entries
- **GIVEN** attraction `A01` "Dragon Rojo" with capacity 20, which `ana@correo.com` entered with an individual ticket and two members entered with a group ticket
- **WHEN** `ATTRACTION STATUS;A01` is processed
- **THEN** the output shows `A01`, `Dragon Rojo`, capacity 20, 3 inside, 17 available and the three e-mails

#### Scenario: Status reflects exits
- **GIVEN** the previous state
- **WHEN** `ATTRACTION EXIT;A01;ana@correo.com` and then `ATTRACTION STATUS;A01` are processed
- **THEN** the output shows 2 inside and 18 available

#### Scenario: Unknown attraction
- **WHEN** `ATTRACTION STATUS;A99` is processed
- **THEN** an error is shown
