# tickets Specification

## Purpose

Defines park tickets — individual and group tickets sharing one abstraction and identifier space — including creation, pricing, discounts, deletion and their retention as historical records.

## Requirements

### Requirement: Common ticket abstraction
Individual and group tickets SHALL share a common abstraction (class extension and/or interface implementation) providing at least identifier, date of use and price, with price behaviour reused rather than duplicated. Operations that do not need the concrete type SHALL use the common abstraction.

#### Scenario: Both ticket kinds share a supertype
- **WHEN** the ticket classes are inspected
- **THEN** individual and group tickets are subtypes of one common ticket type that declares identifier, date and price

### Requirement: Shared identifier space
All tickets, including the individual tickets inside group tickets, SHALL share one identifier space; no two tickets SHALL have the same identifier.

#### Scenario: Individual id clashes with group ticket id
- **WHEN** group ticket `TG01` exists and `TICKET CREATE;TG01;ana@correo.com;<date>` is processed
- **THEN** an error is reported and no ticket is created

### Requirement: Active and historical tickets
A ticket SHALL be active when its date is on or after the current system date and historical otherwise.

#### Scenario: Becomes historical
- **WHEN** a ticket is dated 2027-04-15 and the system date changes from 2027-04-15 to 2027-04-16
- **THEN** the ticket changes from active to historical

### Requirement: Create individual ticket
`TICKET CREATE;<identifier>;<email>;<date>` SHALL create a ticket for an existing user, with a unique identifier, a date on or after the current system date, and the current base price, and SHALL display the price charged. The ticket SHALL reference its user; the user SHALL NOT reference tickets.

#### Scenario: Create with default price
- **WHEN** the system date is 2027-04-15, user Ana exists, SYSTEM PRICE was never used, and `TICKET CREATE;T001;ana@correo.com;2027-04-15` is processed
- **THEN** ticket `T001` is created for Ana and the output shows the price 40.00

#### Scenario: Past date rejected
- **WHEN** the system date is 2027-04-15 and `TICKET CREATE;T002;ana@correo.com;2027-04-14` is processed
- **THEN** an error is reported and no ticket is created

#### Scenario: Unknown user rejected
- **WHEN** `TICKET CREATE` names an email that no user has
- **THEN** an error is reported and no ticket is created

### Requirement: Delete individual ticket
`TICKET DELETE;<identifier>` SHALL delete an individual ticket only if its date is strictly after the current system date and it is not part of a group ticket. Other tickets SHALL be retained.

#### Scenario: Delete future ticket
- **WHEN** ticket `T001` is dated 2027-04-16, the system date is 2027-04-15, and `TICKET DELETE;T001` is processed
- **THEN** `T001` no longer exists

#### Scenario: Today or past ticket retained
- **WHEN** ticket `T001` is dated on or before the system date and `TICKET DELETE;T001` is processed
- **THEN** an error is reported and `T001` still exists

#### Scenario: Composition ticket not deletable alone
- **WHEN** `TICKET DELETE;TG01:12345678Z` is processed for a ticket inside group ticket `TG01`
- **THEN** an error is reported and the ticket still exists

### Requirement: Create group ticket
`GROUPTICKET CREATE;<identifier>;<group>;<date>` SHALL, for an existing non-empty group and a date on or after the system date, create a group ticket composed of one new individual ticket per current member, identified `<identifier>:<dni>` and referencing that member, and SHALL reference the group. All identifiers SHALL be checked first; on any conflict nothing is created.

#### Scenario: Composition created
- **WHEN** group `G01` has Ana (`12345678Z`) and Pedro (`87654321X`) and `GROUPTICKET CREATE;TG01;G01;2027-04-15` is processed on 2027-04-15
- **THEN** group ticket `TG01` exists composed of `TG01:12345678Z` (Ana) and `TG01:87654321X` (Pedro)

#### Scenario: Empty group rejected
- **WHEN** group `G02` has no members and `GROUPTICKET CREATE;TG02;G02;2027-04-15` is processed
- **THEN** an error is reported and no ticket is created

#### Scenario: Derived identifier conflict is atomic
- **WHEN** ticket `TG01:87654321X` already exists and `GROUPTICKET CREATE;TG01;G01;2027-04-15` is processed for a group containing Pedro
- **THEN** an error is reported and neither `TG01` nor any `TG01:<dni>` ticket is created

#### Scenario: Composition is fixed
- **WHEN** group ticket `TG01` was created and later the group's membership changes
- **THEN** the composition of `TG01` is unchanged

### Requirement: Group discount
The discount SHALL depend on the number of members in the purchase: fewer than 3 → 0 %, 3 to 5 → 10 %, 6 to 10 → 20 %, more than 10 → 50 %. It SHALL be applied to the base price of each individual ticket, the total SHALL be the sum of the individual prices, and the total SHALL be displayed.

#### Scenario: Six members at 40 euros
- **WHEN** the base price is 40.00 and a group of 6 users creates a group ticket
- **THEN** each individual ticket costs 32.00 and the displayed total is 192.00

#### Scenario: Bracket boundaries
- **WHEN** group tickets are bought at base price 40.00 by groups of 2, 3, 5, 10 and 11 members
- **THEN** the per-ticket prices are 40.00, 36.00, 36.00, 32.00 and 20.00 respectively

### Requirement: Delete group ticket
`GROUPTICKET DELETE;<identifier>` SHALL delete a group ticket and all individual tickets of its composition only if its date is strictly after the current system date.

#### Scenario: Delete future group ticket
- **WHEN** group ticket `TG01` is dated after the system date and `GROUPTICKET DELETE;TG01` is processed
- **THEN** `TG01` and all `TG01:<dni>` tickets no longer exist

#### Scenario: Today or past group ticket retained
- **WHEN** group ticket `TG01` is dated on or before the system date
- **THEN** `GROUPTICKET DELETE;TG01` is rejected and `TG01` with its composition still exists

### Requirement: Tickets found by traversal
The tickets of a user or a group SHALL be determined by traversing the tickets managed by the park; users and groups SHALL NOT store references to tickets.

#### Scenario: No ticket references in users or groups
- **WHEN** the user and group classes are inspected
- **THEN** neither has a field referring to tickets
