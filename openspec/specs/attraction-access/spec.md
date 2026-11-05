# attraction-access Specification

## Purpose

Defines how tickets give access to attractions: validity of the ticket for the current system date, capacity limits, and the users admitted by a ticket.

## Requirements

### Requirement: ATTRACTION ENTER with an individual ticket
`ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier` SHALL admit into the attraction the user associated with the individual ticket and print `ENTRY GRANTED` with the number of users admitted, when the attraction exists, the ticket exists, the ticket's date equals the current system date and the attraction has an available place for that user.

#### Scenario: Valid ticket and available capacity
- **GIVEN** attraction `A01` with capacity 20 and no occupants, current date 2027-04-15 and Ana's ticket `T001` for 2027-04-15
- **WHEN** `ATTRACTION ENTER;A01;T001` is processed
- **THEN** `ENTRY GRANTED` is printed and Ana is an occupant of `A01`

#### Scenario: Presence independent of later date changes
- **GIVEN** Ana entered `A01` with `T001` on 2027-04-15
- **WHEN** `SYSTEM DATE;2027-04-20` is processed
- **THEN** Ana is still an occupant of `A01`

### Requirement: Entry rejections leave state unchanged
Entry MUST be rejected with an error message starting with `ERROR`, admitting nobody, when the attraction or ticket does not exist, the ticket is not valid for the current system date (earlier or later date), the attraction has no available capacity, or the user is already inside that attraction.

#### Scenario: Ticket not valid today
- **GIVEN** Ana's ticket `T001` for 2027-04-15
- **WHEN** the current date is 2027-04-14 or 2027-04-16 and `ATTRACTION ENTER;A01;T001` is processed
- **THEN** an error message starting with `ERROR` is printed and `A01` has no new occupant

#### Scenario: No available capacity
- **GIVEN** attraction `A02` with capacity 1 already occupied by Pedro, and Ana's valid ticket `T001`
- **WHEN** `ATTRACTION ENTER;A02;T001` is processed
- **THEN** an error message starting with `ERROR` is printed and only Pedro is inside `A02`

#### Scenario: Unknown attraction or ticket
- **WHEN** `ATTRACTION ENTER;A99;T001` or `ATTRACTION ENTER;A01;T999` is processed
- **THEN** an error message starting with `ERROR` is printed

#### Scenario: Already inside
- **GIVEN** Ana is inside `A01`
- **WHEN** she tries to enter `A01` again with a valid ticket
- **THEN** an error message starting with `ERROR` is printed and she appears once in `A01`

### Requirement: Access through the common ticket abstraction
The entry operation SHALL obtain the users to admit from the common ticket abstraction (each ticket type reports the users it admits) instead of using separate access mechanisms per ticket type.

#### Scenario: Ticket type not inspected by the entry operation
- **WHEN** the entry operation is inspected
- **THEN** it asks the ticket for the users it admits and checks validity through the ticket's common date behavior

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
