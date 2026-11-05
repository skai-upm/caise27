# Spec Delta

## Purpose

Defines how tickets give access to attractions: validity of the ticket for the current system date, capacity limits, and the users admitted by a ticket.

## ADDED Requirements

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
