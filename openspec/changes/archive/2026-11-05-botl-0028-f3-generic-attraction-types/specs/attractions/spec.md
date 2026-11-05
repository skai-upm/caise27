## MODIFIED Requirements

### Requirement: Attraction creation
`ATTRACTION CREATE;Identifier;Name;Capacity;Type` SHALL create an attraction with the given identifier, name, maximum capacity and type (`GENERAL`, `INDIVIDUAL`, `GROUP` or `SPECIAL`) and with no users inside, and print a response starting with `OK` that names the type. For compatibility with Delivery 2, `ATTRACTION CREATE;Identifier;Name;Capacity` without a type SHALL remain valid and create a `GENERAL` attraction. The identifier and name MUST be non-empty, the identifier MUST NOT belong to an existing attraction, the capacity MUST be a positive whole number written with digits only, and the type, when given, MUST be one of the four type names; otherwise an `ERROR` response is printed and no attraction is created.

#### Scenario: Attraction created empty
- **WHEN** `ATTRACTION CREATE;A01;Dragon Rojo;20` is processed
- **THEN** a response starting with `OK` is printed and attraction `A01` named `Dragon Rojo` with capacity 20 exists with no users inside

#### Scenario: Duplicate identifier rejected
- **WHEN** attraction `A01` exists and `ATTRACTION CREATE;A01;Otra;5` is processed
- **THEN** an `ERROR` response is printed and `A01` keeps its name and capacity

#### Scenario: Invalid capacity or fields rejected
- **WHEN** `ATTRACTION CREATE;A02;Torre;0`, `ATTRACTION CREATE;A02;Torre;-3`, `ATTRACTION CREATE;A02;Torre;doce`, `ATTRACTION CREATE;A02;Torre;1.5`, `ATTRACTION CREATE;;Torre;12`, `ATTRACTION CREATE;A02;;12` or `ATTRACTION CREATE;A02;Torre` is processed
- **THEN** an `ERROR` response is printed and no attraction `A02` exists

#### Scenario: Attractions of each type
- **WHEN** `ATTRACTION CREATE;A01;Dragon Rojo;20;GENERAL`, `ATTRACTION CREATE;A02;Montana Rusa Individual;12;INDIVIDUAL`, `ATTRACTION CREATE;A03;Rafting Familiar;24;GROUP` and `ATTRACTION CREATE;A04;Pasaje del Terror;15;SPECIAL` are processed
- **THEN** each response starts with `OK` and names the type, and the four attractions exist with those types

#### Scenario: Delivery 2 syntax creates a general attraction
- **WHEN** `ATTRACTION CREATE;A05;Noria;10` is processed
- **THEN** attraction `A05` exists with type `GENERAL`

#### Scenario: Unknown type rejected
- **WHEN** `ATTRACTION CREATE;A06;Torre;10;VIP` or `ATTRACTION CREATE;A06;Torre;10;` is processed
- **THEN** an `ERROR` response is printed and no attraction `A06` exists

### Requirement: Attraction access with an individual ticket
`ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier` with an individual ticket SHALL admit the user associated with the ticket into the attraction and print a response starting with `OK`, provided the attraction exists, the ticket exists, the attraction's type accepts individual tickets (`GENERAL`, `INDIVIDUAL` and `SPECIAL` do; `GROUP` does not), the ticket's date equals the current system date, any condition of the attraction type is satisfied, and the attraction has free capacity for that user. Otherwise it MUST print an `ERROR` response and leave the attraction's occupants unchanged.

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

#### Scenario: Individual ticket rejected by a group attraction
- **WHEN** Ana uses her valid individual ticket `T001` with `ATTRACTION ENTER;A03;T001` and `A03` is a `GROUP` attraction
- **THEN** an `ERROR` response is printed and Ana is not inside `A03`

### Requirement: Group ticket access through the common abstraction
Attraction access SHALL determine who enters from the common ticket abstraction (the users associated with the ticket), with no separate access mechanism per ticket type. `ATTRACTION ENTER` with a group ticket valid on the current system date at an attraction whose type accepts group tickets (`GENERAL` and `GROUP`; not `INDIVIDUAL` nor `SPECIAL`) SHALL admit together all users of its composition who are not already inside, and only if the attraction has free capacity for all of them; otherwise it MUST print an `ERROR` response and nobody enters.

#### Scenario: Whole group enters
- **WHEN** group ticket `TG01` for today is composed of tickets for Ana and Pedro and attraction `A01` has 20 free places, and `ATTRACTION ENTER;A01;TG01` is processed
- **THEN** a response starting with `OK` is printed and both Ana and Pedro are inside `A01`

#### Scenario: Insufficient capacity for the whole group
- **WHEN** attraction `A02` has capacity 1 and `ATTRACTION ENTER;A02;TG01` is processed for a two-member group ticket
- **THEN** an `ERROR` response is printed and neither member is inside `A02`

#### Scenario: Group ticket not valid today
- **WHEN** `TG01` is dated on another day than the current system date
- **THEN** `ATTRACTION ENTER;A01;TG01` is rejected and nobody enters

#### Scenario: Group ticket rejected by individual and special attractions
- **WHEN** `ATTRACTION ENTER;A02;TG01` is processed for `INDIVIDUAL` attraction `A02`, or `ATTRACTION ENTER;A04;TG01` for `SPECIAL` attraction `A04`
- **THEN** an `ERROR` response is printed and no member enters

#### Scenario: Group attraction admits the group
- **WHEN** `ATTRACTION ENTER;A03;TG01` is processed for `GROUP` attraction `A03` with enough free places
- **THEN** a response starting with `OK` is printed and every member is inside `A03`
