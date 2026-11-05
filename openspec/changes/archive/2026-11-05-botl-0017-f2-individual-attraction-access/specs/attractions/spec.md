# Spec Delta

## ADDED Requirements

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
