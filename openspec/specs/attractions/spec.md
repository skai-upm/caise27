# attractions Specification

## Purpose
Defines the park's attractions — unique identifier, name, maximum capacity and the users currently inside — and the commands to create them and list their current occupants.

## Requirements

### Requirement: Attraction creation
`ATTRACTION CREATE;Identifier;Name;Capacity` SHALL create an attraction with the given identifier, name and maximum capacity and with no users inside, and print a response starting with `OK`. The identifier and name MUST be non-empty, the identifier MUST NOT belong to an existing attraction, and the capacity MUST be a positive whole number written with digits only; otherwise an `ERROR` response is printed and no attraction is created.

#### Scenario: Attraction created empty
- **WHEN** `ATTRACTION CREATE;A01;Dragon Rojo;20` is processed
- **THEN** a response starting with `OK` is printed and attraction `A01` named `Dragon Rojo` with capacity 20 exists with no users inside

#### Scenario: Duplicate identifier rejected
- **WHEN** attraction `A01` exists and `ATTRACTION CREATE;A01;Otra;5` is processed
- **THEN** an `ERROR` response is printed and `A01` keeps its name and capacity

#### Scenario: Invalid capacity or fields rejected
- **WHEN** `ATTRACTION CREATE;A02;Torre;0`, `ATTRACTION CREATE;A02;Torre;-3`, `ATTRACTION CREATE;A02;Torre;doce`, `ATTRACTION CREATE;A02;Torre;1.5`, `ATTRACTION CREATE;;Torre;12`, `ATTRACTION CREATE;A02;;12` or `ATTRACTION CREATE;A02;Torre` is processed
- **THEN** an `ERROR` response is printed and no attraction `A02` exists

### Requirement: One-way occupant relationship
An attraction SHALL hold references to the users currently inside it; users MUST NOT hold any reference to the attraction they are in.

#### Scenario: Occupants reachable from the attraction only
- **WHEN** a user is inside attraction `A01`
- **THEN** the user is reachable through `A01`'s occupants and the user object has no attraction reference

### Requirement: Listing users inside an attraction
`ATTRACTION LIST;AttractionIdentifier` SHALL print the email addresses of the users currently inside the attraction, or a controlled response stating that there are no users inside when it is empty. It MUST NOT change the attraction's state. An unknown identifier MUST produce an `ERROR` response.

#### Scenario: Empty attraction
- **WHEN** attraction `A01` has no users inside and `ATTRACTION LIST;A01` is processed
- **THEN** a response starting with `OK` stating that no users are inside is printed and `A01` still has no users inside

#### Scenario: Occupants listed by email
- **WHEN** Ana (`ana@correo.com`) and Pedro (`pedro@correo.com`) are inside `A01` and `ATTRACTION LIST;A01` is processed
- **THEN** the output contains `ana@correo.com` and `pedro@correo.com` and the occupants are unchanged

#### Scenario: Unknown attraction
- **WHEN** `ATTRACTION LIST;A99` is processed and no such attraction exists
- **THEN** an `ERROR` response is printed

### Requirement: Attraction commands listed in HELP
The `HELP` output SHALL list `ATTRACTION CREATE;<id>;<name>;<capacity>` and `ATTRACTION LIST;<id>`.

#### Scenario: HELP shows attraction commands
- **WHEN** `HELP` is processed
- **THEN** the output contains both attraction command syntaxes

### Requirement: Attraction access with an individual ticket
`ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier` with an individual ticket SHALL admit the user associated with the ticket into the attraction and print a response starting with `OK`, provided the attraction exists, the ticket exists, the ticket's date equals the current system date, and the attraction has free capacity for that user. Otherwise it MUST print an `ERROR` response and leave the attraction's occupants unchanged.

#### Scenario: Valid ticket and free capacity
- **WHEN** the system date is 2027-04-15, Ana holds individual ticket `T001` for 2027-04-15, attraction `A01` has capacity 20 and is empty, and `ATTRACTION ENTER;A01;T001` is processed
- **THEN** a response starting with `OK` is printed and Ana is inside `A01`

#### Scenario: Ticket not valid today
- **WHEN** `T001` is dated 2027-04-16 or 2027-04-14 and the system date is 2027-04-15
- **THEN** `ATTRACTION ENTER;A01;T001` is rejected with an `ERROR` response and nobody enters

#### Scenario: No free capacity
- **WHEN** attraction `A02` has capacity 1 and already contains Pedro, and Ana uses her valid ticket to enter `A02`
- **THEN** an `ERROR` response is printed and Ana is not inside `A02`

#### Scenario: Unknown attraction or ticket
- **WHEN** `ATTRACTION ENTER;A99;T001` or `ATTRACTION ENTER;A01;T999` is processed
- **THEN** an `ERROR` response is printed and nobody enters

#### Scenario: Already inside
- **WHEN** Ana is already inside `A01` and `ATTRACTION ENTER;A01;T001` is processed again
- **THEN** an `ERROR` response is printed and Ana appears only once among `A01`'s occupants

### Requirement: Presence independent of later date changes
The ticket date SHALL be checked only at entry; once a user is inside an attraction, their presence MUST NOT depend on subsequent changes to the system date.

#### Scenario: Date moves on after entry
- **WHEN** Ana entered `A01` on 2027-04-15 and then `SYSTEM DATE;2027-04-16` is processed
- **THEN** `ATTRACTION LIST;A01` still shows `ana@correo.com`

### Requirement: Access through the common ticket abstraction
Attraction access SHALL determine who enters from the common ticket abstraction (the users associated with the ticket) rather than through separate access mechanisms per ticket type. Until group-ticket access is introduced, using a group ticket with `ATTRACTION ENTER` MUST be rejected with an `ERROR` response and nobody enters.

#### Scenario: Group ticket not yet accepted
- **WHEN** `ATTRACTION ENTER;A01;TG01` is processed with group ticket `TG01` valid today
- **THEN** an `ERROR` response is printed and nobody enters

### Requirement: Attraction access listed in HELP
The `HELP` output SHALL list `ATTRACTION ENTER;<attraction>;<ticket>`.

#### Scenario: HELP shows attraction access
- **WHEN** `HELP` is processed
- **THEN** the output contains `ATTRACTION ENTER;<attraction>;<ticket>`
