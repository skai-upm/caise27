## ADDED Requirements

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
