# ticket-management Specification

## Purpose

Defines park tickets: the properties shared by every ticket type (unique identifier in a common identifier space, date of use, price), individual ticket purchase and deletion, and the distinction between active and historical tickets.

## Requirements

### Requirement: Common ticket properties
Every ticket SHALL have an identifier, a date of use and a price, and all ticket types SHALL share one identifier space: no two tickets of any type SHALL have the same identifier.

#### Scenario: Identifier shared across tickets
- **WHEN** ticket `T001` exists and another ticket is created with identifier `T001`
- **THEN** the creation is rejected with an error and no ticket is created

### Requirement: Individual ticket purchase
`TICKET CREATE;<identifier>;<email>;<date>` SHALL create an individual ticket for the existing user with that email, valid only on the given `yyyy-MM-dd` date, priced at the current base price, and SHALL display the price charged. The ticket SHALL reference its user; the user SHALL NOT reference its tickets.

#### Scenario: Ticket at default price
- **WHEN** the system date is `2027-04-01`, `ana@correo.com` exists and `TICKET CREATE;T001;ana@correo.com;2027-04-15` is processed without a previous `SYSTEM PRICE`
- **THEN** ticket `T001` for Ana on 2027-04-15 is created and the output shows the price `40.00`

#### Scenario: Ticket at configured price
- **WHEN** `SYSTEM PRICE;45.50` was processed and then `TICKET CREATE;T002;ana@correo.com;2027-04-15`
- **THEN** the output shows the price `45.50`

#### Scenario: Price fixed at creation
- **WHEN** ticket `T001` was created at `40.00` and then `SYSTEM PRICE;45.50` is processed
- **THEN** ticket `T001` still has price `40.00`

#### Scenario: Ticket for the current date
- **WHEN** the system date is `2027-04-15` and `TICKET CREATE;T003;ana@correo.com;2027-04-15` is processed
- **THEN** the ticket is created

#### Scenario: Past date rejected
- **WHEN** the system date is `2027-04-15` and `TICKET CREATE;T004;ana@correo.com;2027-04-14` is processed
- **THEN** an error is printed and no ticket is created

#### Scenario: Unknown user or invalid input rejected
- **WHEN** `TICKET CREATE;T005;nadie@correo.com;2027-04-15`, `TICKET CREATE;T005;ana@correo.com;15/04/2027` or `TICKET CREATE;T005;ana@correo.com` is processed
- **THEN** an error is printed and no ticket is created

### Requirement: Individual ticket deletion
`TICKET DELETE;<identifier>` SHALL delete an individual ticket only when its date is strictly later than the current system date. Tickets dated on the current day or earlier SHALL NOT be deleted and SHALL be kept as historical records. Individual tickets that belong to a group ticket SHALL NOT be deleted with `TICKET DELETE`.

#### Scenario: Future ticket deleted
- **WHEN** the system date is `2027-04-01`, ticket `T001` is dated `2027-04-15` and `TICKET DELETE;T001` is processed
- **THEN** a confirmation is printed and `T001` no longer exists

#### Scenario: Ticket for today kept
- **WHEN** the system date is `2027-04-15`, ticket `T001` is dated `2027-04-15` and `TICKET DELETE;T001` is processed
- **THEN** an error is printed and `T001` still exists

#### Scenario: Past ticket kept
- **WHEN** ticket `T001` is dated `2027-04-15`, the system date is later set to `2027-04-20` and `TICKET DELETE;T001` is processed
- **THEN** an error is printed and `T001` still exists

#### Scenario: Unknown ticket
- **WHEN** `TICKET DELETE;T999` is processed and no such ticket exists
- **THEN** an error is printed

### Requirement: Active and historical tickets
A ticket SHALL be active when its date is equal to or later than the current system date and historical otherwise. Ticket ownership SHALL be determined by traversing the tickets managed by the system, not stored on users.

#### Scenario: Ticket becomes historical
- **WHEN** ticket `T001` is dated `2027-04-15` and the system date changes from `2027-04-15` to `2027-04-16`
- **THEN** `T001` changes from active to historical

### Requirement: Group ticket purchase
`GROUPTICKET CREATE;<identifier>;<group>;<date>` SHALL create a group ticket for an existing, non-empty group, for a date equal to or later than the current system date, with an identifier unique among all tickets. The group ticket SHALL be composed of one new individual ticket per user belonging to the group at that moment, each identified `<groupTicketIdentifier>:<dni>` and referencing its user, and SHALL reference the purchasing group. The application SHALL display the total price charged.

#### Scenario: Group ticket created
- **WHEN** group `G01` contains Ana (DNI `12345678Z`) and Pedro (DNI `87654321X`), the system date is `2027-04-01` and `GROUPTICKET CREATE;TG01;G01;2027-04-15` is processed
- **THEN** group ticket `TG01` for `G01` is created with individual tickets `TG01:12345678Z` and `TG01:87654321X`, and the total `80.00` is displayed

#### Scenario: Empty group rejected
- **WHEN** group `G02` has no members and `GROUPTICKET CREATE;TG02;G02;2027-04-15` is processed
- **THEN** an error is printed and no ticket is created

#### Scenario: Past date or unknown group rejected
- **WHEN** the system date is `2027-04-15` and `GROUPTICKET CREATE;TG03;G01;2027-04-14` or `GROUPTICKET CREATE;TG03;G99;2027-04-15` is processed
- **THEN** an error is printed and no ticket is created

#### Scenario: Derived identifier conflict prevents partial creation
- **WHEN** ticket `TG04:87654321X` already exists and `GROUPTICKET CREATE;TG04;G01;2027-04-15` is processed for a group containing Ana and Pedro
- **THEN** an error is printed and neither `TG04` nor `TG04:12345678Z` is created

#### Scenario: Composition fixed at purchase
- **WHEN** group ticket `TG01` was created for Ana and Pedro and later, once `TG01` is historical, Pedro is removed from `G01`
- **THEN** `TG01` is still composed of the tickets of Ana and Pedro

### Requirement: Group discount
Each individual ticket of a group ticket SHALL cost the current base price reduced by a discount that depends on the number of users included: fewer than 3 → 0%, 3 to 5 → 10%, 6 to 10 → 20%, more than 10 → 50%. The group ticket total SHALL be the sum of the prices of its individual tickets.

#### Scenario: Six members at 40 euros
- **WHEN** the base price is `40.00` and a group of 6 users buys `GROUPTICKET CREATE;TG02;G02;2027-04-15`
- **THEN** each of the six individual tickets costs `32.00` and the total displayed is `192.00`

#### Scenario: Bracket limits
- **WHEN** group tickets are bought at base price `40.00` by groups of 2, 3, 5, 10 and 11 users
- **THEN** the per-ticket prices are `40.00`, `36.00`, `36.00`, `32.00` and `20.00` respectively

### Requirement: Group ticket deletion
`GROUPTICKET DELETE;<identifier>` SHALL delete a group ticket only when its date is strictly later than the current system date, together with the individual tickets of its composition. Group tickets dated on the current day or earlier SHALL be kept as historical records. Individual tickets of a group ticket SHALL NOT be deleted with `TICKET DELETE`.

#### Scenario: Future group ticket deleted
- **WHEN** the system date is `2027-04-01`, `TG01` is dated `2027-04-15` and `GROUPTICKET DELETE;TG01` is processed
- **THEN** a confirmation is printed and `TG01`, `TG01:12345678Z` and `TG01:87654321X` no longer exist

#### Scenario: Group ticket for today kept
- **WHEN** the system date is `2027-04-15`, `TG01` is dated `2027-04-15` and `GROUPTICKET DELETE;TG01` is processed
- **THEN** an error is printed and `TG01` still exists

#### Scenario: Composed ticket not deletable alone
- **WHEN** `TICKET DELETE;TG01:12345678Z` is processed
- **THEN** an error is printed and the ticket still exists
