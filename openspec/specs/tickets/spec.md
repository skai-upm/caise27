# tickets Specification

## Purpose

Defines park tickets: the information and behavior shared by every ticket type, the single identifier space for all tickets, and the purchase and deletion of individual tickets.

## Requirements

### Requirement: Common ticket abstraction
Every ticket SHALL have a unique identifier, a date of use and a price. Individual and group tickets SHALL share this information and their price-related behavior through a common abstraction, so that operations that do not need the concrete type handle any ticket uniformly.

#### Scenario: Any ticket handled through the common abstraction
- **GIVEN** tickets of the available types
- **WHEN** an operation lists or inspects tickets without knowing their type
- **THEN** it can obtain each ticket's identifier, date and price

### Requirement: Shared ticket identifier space
Ticket identifiers SHALL be unique among all tickets in the system regardless of type, and this uniqueness SHALL be checked in one common way for every ticket type.

#### Scenario: Identifier already used
- **GIVEN** a ticket `T001` exists
- **WHEN** another ticket with identifier `T001` is requested
- **THEN** an error message starting with `ERROR` is printed and no ticket is created

### Requirement: TICKET CREATE command
`TICKET CREATE;<identifier>;<email>;<date>` SHALL create an individual ticket for the registered user with that email, valid only on `<date>` (`yyyy-MM-dd`), priced at the current base price, and print `TICKET CREATED` together with the price charged. It MUST be rejected with an error message starting with `ERROR` when the identifier is empty or already used, the user does not exist, or the date is invalid or earlier than the current system date. The ticket SHALL reference its user; the user SHALL NOT reference its tickets.

#### Scenario: Ticket at the initial price
- **GIVEN** a registered user `ana@correo.com`, current date 2027-04-01 and no SYSTEM PRICE used
- **WHEN** `TICKET CREATE;T001;ana@correo.com;2027-04-15` is processed
- **THEN** `TICKET CREATED` is printed with the price `40.00` and ticket `T001` for Ana on 2027-04-15 exists

#### Scenario: Ticket at the configured price
- **GIVEN** `SYSTEM PRICE;45.50` was processed
- **WHEN** a valid `TICKET CREATE` is processed
- **THEN** the price shown and stored is `45.50`

#### Scenario: Ticket for today allowed
- **GIVEN** current date 2027-04-15
- **WHEN** `TICKET CREATE;T002;ana@correo.com;2027-04-15` is processed
- **THEN** `TICKET CREATED` is printed

#### Scenario: Past date or unknown user rejected
- **GIVEN** current date 2027-04-15
- **WHEN** `TICKET CREATE;T003;ana@correo.com;2027-04-14` or `TICKET CREATE;T004;nadie@correo.com;2027-04-20` is processed
- **THEN** an error message starting with `ERROR` is printed and no ticket is created

#### Scenario: Later price changes do not alter existing tickets
- **GIVEN** ticket `T001` created at price 40.00
- **WHEN** `SYSTEM PRICE;50` is processed
- **THEN** `T001` still has price 40.00

### Requirement: TICKET DELETE command
`TICKET DELETE;<identifier>` SHALL delete an individual ticket and print `TICKET DELETED` only when its date is strictly later than the current system date. Tickets dated on the current day or earlier MUST NOT be deleted and are retained as historical records; the request is rejected with an error message starting with `ERROR`, as is an unknown identifier.

#### Scenario: Future ticket deleted
- **GIVEN** current date 2027-04-01 and ticket `T001` on 2027-04-15
- **WHEN** `TICKET DELETE;T001` is processed
- **THEN** `TICKET DELETED` is printed and `T001` no longer exists

#### Scenario: Ticket for today or the past not deleted
- **GIVEN** ticket `T001` on 2027-04-15
- **WHEN** the current date is 2027-04-15 (or later) and `TICKET DELETE;T001` is processed
- **THEN** an error message starting with `ERROR` is printed and `T001` still exists

#### Scenario: Unknown ticket
- **WHEN** `TICKET DELETE;NOPE` is processed
- **THEN** an error message starting with `ERROR` is printed

### Requirement: GROUPTICKET CREATE command
`GROUPTICKET CREATE;<identifier>;<group>;<date>` SHALL create a group ticket for an existing, non-empty group, valid on `<date>` (not earlier than the current system date), composed of one new individual ticket per user belonging to the group at that moment, each identified `<identifier>:<dni>` and referencing its user. On success it SHALL print `GROUP TICKET CREATED` with the total price. It MUST be rejected with an error message starting with `ERROR` when the group does not exist or is empty, or the date is invalid or past.

#### Scenario: Composition created with derived identifiers
- **GIVEN** group `G01` with Ana (DNI `12345678Z`) and Pedro (DNI `87654321X`), current date 2027-04-01
- **WHEN** `GROUPTICKET CREATE;TG01;G01;2027-04-15` is processed
- **THEN** `GROUP TICKET CREATED` is printed and tickets `TG01`, `TG01:12345678Z` and `TG01:87654321X` exist, the two individual tickets referencing Ana and Pedro and dated 2027-04-15

#### Scenario: Empty group rejected
- **GIVEN** an empty group `G03`
- **WHEN** `GROUPTICKET CREATE;TG03;G03;2027-04-15` is processed
- **THEN** an error message starting with `ERROR` is printed and no ticket is created

#### Scenario: Composition fixed at purchase
- **GIVEN** group ticket `TG01` purchased when `G01` had Ana and Pedro, later historical
- **WHEN** the group's membership changes
- **THEN** `TG01` is still composed of the tickets of Ana and Pedro

### Requirement: Atomic identifier check for group tickets
Before a group ticket is created, its identifier and every derived composition identifier SHALL be checked against all existing tickets; if any conflicts, no part of the group ticket MUST be created.

#### Scenario: Derived identifier conflict
- **GIVEN** an existing individual ticket `TG01:12345678Z`
- **WHEN** `GROUPTICKET CREATE;TG01;G01;2027-04-15` is processed for a group containing Ana
- **THEN** an error message starting with `ERROR` is printed, no ticket `TG01` exists and no other `TG01:` ticket was added

#### Scenario: Group ticket identifier conflict
- **GIVEN** an existing ticket `T001`
- **WHEN** `GROUPTICKET CREATE;T001;G01;2027-04-15` is processed
- **THEN** an error message starting with `ERROR` is printed

### Requirement: Group discounts
Each composition ticket SHALL cost the current base price minus a discount determined by the number of members included: fewer than 3 → 0%, 3 to 5 → 10%, 6 to 10 → 20%, more than 10 → 50%. The group ticket's total price SHALL be the sum of its composition tickets' prices.

#### Scenario: Six members at 40 euros
- **GIVEN** a group of 6 users and current base price 40.00
- **WHEN** its group ticket is created
- **THEN** each of the 6 individual tickets costs 32.00 and the displayed total is 192.00

#### Scenario: Bracket boundaries
- **WHEN** group tickets are created at base price 40.00 for groups of 2, 3, 5, 10 and 11 members
- **THEN** the per-ticket prices are 40.00, 36.00, 36.00, 32.00 and 20.00 respectively

### Requirement: GROUPTICKET DELETE command
`GROUPTICKET DELETE;<identifier>` SHALL delete a group ticket together with all individual tickets of its composition and print `GROUP TICKET DELETED`, only when its date is strictly later than the current system date. Group tickets dated today or earlier MUST be retained as history and the request rejected with an error message starting with `ERROR`, as is an unknown identifier.

#### Scenario: Future group ticket deleted with its composition
- **GIVEN** group ticket `TG01` on 2027-04-15 and current date 2027-04-01
- **WHEN** `GROUPTICKET DELETE;TG01` is processed
- **THEN** `GROUP TICKET DELETED` is printed and neither `TG01` nor any `TG01:` ticket exists

#### Scenario: Today's group ticket kept
- **GIVEN** group ticket `TG01` on 2027-04-15 and current date 2027-04-15
- **WHEN** `GROUPTICKET DELETE;TG01` is processed
- **THEN** an error message starting with `ERROR` is printed and `TG01` still exists

### Requirement: Composition tickets not deleted individually
An individual ticket that belongs to a group ticket's composition MUST NOT be deleted with `TICKET DELETE`; the request is rejected with an error message starting with `ERROR`.

#### Scenario: TICKET DELETE on a composition ticket
- **GIVEN** future group ticket `TG01` containing `TG01:12345678Z`
- **WHEN** `TICKET DELETE;TG01:12345678Z` is processed
- **THEN** an error message starting with `ERROR` is printed and the ticket still exists

### Requirement: Ticket deletion removes associated passes
When a future individual ticket is deleted with `TICKET DELETE`, all SpeedPasses and the Special Pass associated with it SHALL be deleted too, whatever purchase created them. When a future group ticket is deleted with `GROUPTICKET DELETE`, the passes of every composition ticket, the composition tickets and the group ticket SHALL be deleted together; if the deletion is not allowed nothing is removed.

#### Scenario: Individual ticket with passes deleted
- **GIVEN** future ticket `T001` with `SP01:1`, `SP02:1`…`SP02:10` and `PE01`
- **WHEN** `TICKET DELETE;T001` is processed
- **THEN** `TICKET DELETED` is printed and none of those passes exists

#### Scenario: Group ticket deletion with member passes
- **GIVEN** future group ticket `TG01` whose composition ticket `TG01:12345678Z` has SpeedPass `SP03:1`
- **WHEN** `GROUPTICKET DELETE;TG01` is processed
- **THEN** `SP03:1`, the composition tickets and `TG01` no longer exist

#### Scenario: Rejected deletion keeps passes
- **GIVEN** ticket `T001` dated today with SpeedPass `SP01:1`
- **WHEN** `TICKET DELETE;T001` is processed
- **THEN** an error message starting with `ERROR` is printed and `SP01:1` still exists
