# attractions Specification

## Purpose

Defines the park's attractions, how visitors enter and leave them with individual or group tickets under date and capacity rules, and how their current status is queried.

## Requirements

### Requirement: Attraction data
An attraction SHALL have a unique non-empty identifier, a non-empty name, a maximum capacity that is a positive integer, a type (GENERAL, INDIVIDUAL, GROUP or SPECIAL) and the collection of users currently present. Attractions are empty whenever the application starts. `ATTRACTION CREATE;<id>;<name>;<capacity>` creates a GENERAL attraction and `ATTRACTION CREATE;<id>;<name>;<capacity>;<type>` one of the given type (matched ignoring case); an unknown type is rejected with `ERROR INVALID ATTRACTION TYPE`.

#### Scenario: Create attraction
- **WHEN** `ATTRACTION CREATE;A01;Tornado;20` is processed
- **THEN** the response is `OK ATTRACTION CREATED` and A01 exists with capacity 20, type GENERAL and no users present

#### Scenario: Invalid attraction data
- **WHEN** `ATTRACTION CREATE;A02;Noria;0`, `ATTRACTION CREATE;A02;Noria;muchos` or `ATTRACTION CREATE;A01;Otra;5` (A01 exists) is processed
- **THEN** the responses are `ERROR INVALID CAPACITY`, `ERROR INVALID CAPACITY` and `ERROR DUPLICATE ATTRACTION`

#### Scenario: Typed attraction
- **WHEN** `ATTRACTION CREATE;A03;Barcas;6;group` is processed
- **THEN** the response is `OK ATTRACTION CREATED` and A03 has type GROUP

#### Scenario: Unknown type
- **WHEN** `ATTRACTION CREATE;A04;Noria;6;VIP` is processed
- **THEN** the response is `ERROR INVALID ATTRACTION TYPE`

### Requirement: Delete attraction
`ATTRACTION DELETE;<id>` SHALL delete an attraction with nobody inside and respond `OK ATTRACTION DELETED`; an attraction with users present is kept with `ERROR ATTRACTION NOT EMPTY`, and an unknown identifier gives `ERROR ATTRACTION NOT FOUND`.

#### Scenario: Occupied attraction
- **WHEN** a user is inside A01 and `ATTRACTION DELETE;A01` is processed
- **THEN** the response is `ERROR ATTRACTION NOT EMPTY`

### Requirement: Enter with a ticket
`ATTRACTION ENTER;<attractionId>;<ticketId>` SHALL add every user admitted by the ticket — the holder of an individual ticket, or the holders of all individual tickets composing a group ticket — to the attraction, handling both ticket kinds through the same abstraction, and respond `OK ENTERED <n> USERS`. It SHALL reject an unknown attraction (`ERROR ATTRACTION NOT FOUND`), an unknown ticket (`ERROR TICKET NOT FOUND`), a ticket of a kind the attraction type does not admit (`ERROR TICKET TYPE NOT ACCEPTED`), a ticket whose date is not the system date (`ERROR TICKET NOT VALID TODAY`) and a ticket whose users are already inside an attraction (`ERROR USER ALREADY INSIDE`).

#### Scenario: Individual entry
- **WHEN** the system date is 2026-10-15 and `ATTRACTION ENTER;A01;T001` is processed for Ana's ticket dated 2026-10-15
- **THEN** the response is `OK ENTERED 1 USERS` and Ana is present in A01

#### Scenario: Group entry
- **WHEN** `ATTRACTION ENTER;A01;TG01` is processed for a group ticket of 3 members dated on the system date
- **THEN** the response is `OK ENTERED 3 USERS` and the three members are present

#### Scenario: Ticket for another day
- **WHEN** the system date is 2026-10-14 and `ATTRACTION ENTER;A01;T001` is processed for a ticket dated 2026-10-15
- **THEN** the response is `ERROR TICKET NOT VALID TODAY` and nobody enters

#### Scenario: Ticket kind not admitted
- **WHEN** an individual ticket is used at a GROUP attraction, or a group ticket at an INDIVIDUAL or SPECIAL attraction
- **THEN** the response is `ERROR TICKET TYPE NOT ACCEPTED` and nobody enters

### Requirement: Capacity is never exceeded
An entry SHALL be rejected with `ERROR CAPACITY EXCEEDED` when the users of the ticket do not all fit in the available places; group entry is atomic, so either all members enter or none does.

#### Scenario: Group does not fit
- **WHEN** 3 places remain in A01 and `ATTRACTION ENTER;A01;TG01` is processed for a group ticket of 4 users
- **THEN** the response is `ERROR CAPACITY EXCEEDED` and no member enters

### Requirement: Exit with a ticket
`ATTRACTION EXIT;<attractionId>;<ticketId>` SHALL remove from the attraction the users of that ticket who are present, freeing their places, and respond `OK EXITED <n> USERS`; the date is not checked on exit. If none of the ticket's users is inside, the response is `ERROR NOT INSIDE`.

#### Scenario: Exit frees capacity
- **WHEN** Ana entered A01 with T001 and `ATTRACTION EXIT;A01;T001` is processed
- **THEN** the response is `OK EXITED 1 USERS` and the available places of A01 increase by one

### Requirement: Attraction status
`ATTRACTION STATUS;<attractionId>` SHALL display the attraction's identifier, name, type, maximum capacity, number of users present, number of available places and the users present (name, surname(s) and email), reflecting the actual state at that moment regardless of whether users entered individually or with a group ticket.

#### Scenario: Status after entries and exit
- **WHEN** T001 (1 user) and TG01 (3 users) entered A01 with capacity 20, and then T001 exits
- **THEN** `ATTRACTION STATUS;A01` shows capacity 20, 3 present, 17 available, and the three members of TG01

#### Scenario: Status shows the type
- **WHEN** `ATTRACTION STATUS;A03` is processed for a GROUP attraction
- **THEN** the output contains the line `Type: GROUP`

### Requirement: Attraction persistence
Attraction definitions SHALL be stored in a text file (`data/attractions.txt` by default) as `<id>;<name>;<capacity>;<type>`, rewritten after every create or delete and loaded at startup; lines without a type load as GENERAL; the users present are not persisted.

#### Scenario: Restored empty
- **WHEN** an attraction with users inside exists and the application is restarted
- **THEN** the attraction exists with the same name and capacity and nobody inside

#### Scenario: Type restored
- **WHEN** a SPECIAL attraction is created and the application is restarted
- **THEN** the attraction is still SPECIAL

### Requirement: Attraction types
GENERAL attractions SHALL admit individual and group tickets; INDIVIDUAL only individual tickets; GROUP only group tickets; SPECIAL only individual tickets, on which the Special Pass rules are applied. The accepted kind SHALL be expressed through access-capability interfaces implemented by the tickets and a bounded generic type parameter of the attraction, which is used to check and convert the ticket before admitting it.

#### Scenario: General admits both
- **WHEN** an individual ticket and a group ticket valid today enter a GENERAL attraction
- **THEN** both entries succeed

#### Scenario: Special uses individual tickets
- **WHEN** an individual ticket valid today enters a SPECIAL attraction
- **THEN** the entry is evaluated on that individual ticket
