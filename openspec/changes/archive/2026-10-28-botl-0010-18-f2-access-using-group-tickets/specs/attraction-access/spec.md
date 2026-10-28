## ADDED Requirements

### Requirement: ATTRACTION ENTER with a group ticket
When the ticket used with `ATTRACTION ENTER` is a group ticket valid for the current system date, all users of the individual tickets in its composition SHALL enter the attraction together. Access SHALL be indivisible: if the attraction lacks capacity for all of them, or any of them is already inside, none SHALL enter and an error message SHALL be displayed.

#### Scenario: Whole group enters
- **WHEN** group ticket `TG01` for today has 3 members and attraction `A01` has 3 free places, and `ATTRACTION ENTER;A01;TG01` is processed
- **THEN** all 3 members are inside `A01`

#### Scenario: Insufficient capacity for the group
- **WHEN** group ticket `TG01` for today has 3 members and attraction `A01` has 2 free places, and `ATTRACTION ENTER;A01;TG01` is processed
- **THEN** an error message is displayed and no member enters `A01`

#### Scenario: Group ticket for another date
- **WHEN** group ticket `TG01` is dated tomorrow and `ATTRACTION ENTER;A01;TG01` is processed
- **THEN** an error message is displayed and nobody enters

### Requirement: ATTRACTION EXIT command
The system SHALL accept `ATTRACTION EXIT;AttractionIdentifier;Email` and remove the user with that e-mail from the attraction's occupants, releasing one place. Users who entered with a group ticket SHALL leave individually. An unknown attraction or a user not inside it SHALL be rejected with an error message.

#### Scenario: User leaves
- **WHEN** `ana@correo.com` is inside `A01` and `ATTRACTION EXIT;A01;ana@correo.com` is processed
- **THEN** `ana@correo.com` is no longer inside `A01` and one more place is available

#### Scenario: Group member leaves alone
- **WHEN** three users entered `A01` with a group ticket and one of them exits with her e-mail
- **THEN** the other two remain inside `A01`

#### Scenario: User not inside
- **WHEN** `pedro@correo.com` is not inside `A01` and `ATTRACTION EXIT;A01;pedro@correo.com` is processed
- **THEN** an error message is displayed and the occupants of `A01` are unchanged

### Requirement: ATTRACTION STATUS command
The system SHALL accept `ATTRACTION STATUS;AttractionIdentifier` and display, from the attraction's own state, its identifier, name, maximum capacity, number of users inside, number of available places and the users inside, regardless of whether they entered with individual or group tickets. An unknown attraction SHALL be reported with an error message.

#### Scenario: Status after entries and exits
- **WHEN** attraction `A01` (Dragon Rojo, capacity 20) holds 3 users who entered with a group ticket and 1 with an individual ticket, and one of them exits, and `ATTRACTION STATUS;A01` is processed
- **THEN** the output shows `A01`, `Dragon Rojo`, capacity 20, 3 users inside, 17 available places and the e-mails of the 3 users inside
