## ADDED Requirements

### Requirement: Atomic entry with a group ticket
`ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier` with a group ticket valid for the current system date SHALL admit together all users associated with the individual tickets of its composition and print `ENTRY GRANTED` with the number admitted. If the attraction lacks available places for all of them, or any of them is already inside, the entry MUST be rejected with an error message starting with `ERROR` and no member enters.

#### Scenario: Enough capacity for all members
- **GIVEN** group ticket `TG01` for today composed of Ana and Pedro, and attraction `A01` with 20 free places
- **WHEN** `ATTRACTION ENTER;A01;TG01` is processed
- **THEN** `ENTRY GRANTED` reporting 2 users is printed and both are inside `A01`

#### Scenario: Insufficient capacity admits nobody
- **GIVEN** group ticket `TG01` for today with 2 members and attraction `A02` with 1 free place
- **WHEN** `ATTRACTION ENTER;A02;TG01` is processed
- **THEN** an error message starting with `ERROR` is printed and no member is inside `A02`

#### Scenario: Group ticket for another date
- **GIVEN** group ticket `TG01` for 2027-04-15 and current date 2027-04-16
- **WHEN** `ATTRACTION ENTER;A01;TG01` is processed
- **THEN** an error message starting with `ERROR` is printed and nobody enters

### Requirement: ATTRACTION EXIT command
`ATTRACTION EXIT;AttractionIdentifier;Email` SHALL remove the user with that email from the attraction's occupants, freeing one place, and print `USER LEFT ATTRACTION`. Users who entered with a group ticket SHALL leave individually. An unknown attraction or a user not inside it is rejected with an error message starting with `ERROR`.

#### Scenario: Group member leaves individually
- **GIVEN** Ana and Pedro entered `A01` with group ticket `TG01`
- **WHEN** `ATTRACTION EXIT;A01;ana@correo.com` is processed
- **THEN** `USER LEFT ATTRACTION` is printed, Pedro remains inside and one more place is available

#### Scenario: User not inside
- **WHEN** `ATTRACTION EXIT;A01;nadie@correo.com` or `ATTRACTION EXIT;A99;ana@correo.com` is processed
- **THEN** an error message starting with `ERROR` is printed

### Requirement: ATTRACTION STATUS command
`ATTRACTION STATUS;AttractionIdentifier` SHALL display the attraction's identifier, name, maximum capacity, number of users inside, number of available places and the users inside (by email), obtained from the attraction's own state and identical regardless of whether users entered with individual or group tickets. It is read-only; an unknown identifier is rejected with an error message starting with `ERROR`.

#### Scenario: Status after mixed entries and an exit
- **GIVEN** attraction `A01` (capacity 20) where Ana and Pedro entered with `TG01`, Luis entered with `T003`, and Ana left
- **WHEN** `ATTRACTION STATUS;A01` is processed
- **THEN** it shows `A01`, `Dragon Rojo`, capacity 20, 2 users inside, 18 available places and the emails of Pedro and Luis
