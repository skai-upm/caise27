# tickets Specification

## Purpose

Defines individual and group tickets: a shared identifier space, pricing from the current base price with group discounts, and the rules for creating and deleting tickets.

## Requirements

### Requirement: Shared ticket identifier space
Every ticket, including individual tickets inside a group ticket, SHALL have an identifier unique among all tickets of any type. A creation whose identifier (or any derived identifier) is already used SHALL be rejected entirely.

#### Scenario: Identifier reused across types
- **WHEN** individual ticket T001 exists and `GROUPTICKET CREATE;T001;G01;2027-04-15` is processed
- **THEN** an error is printed and no ticket is created

### Requirement: Create individual ticket
`TICKET CREATE;<identifier>;<email>;<date>` SHALL create a ticket for an existing user, dated on or after the current system date, priced at the current base price, and print the price. Unknown users, past or malformed dates and duplicate identifiers SHALL be rejected.

#### Scenario: Create ticket
- **WHEN** the system date is 2027-04-15 and `TICKET CREATE;T001;ana@correo.com;2027-04-15` is processed
- **THEN** ticket T001 for Ana is created and a price of 40.00 euros is displayed

#### Scenario: Past date rejected
- **WHEN** the system date is 2027-04-15 and `TICKET CREATE;T002;ana@correo.com;2027-04-14` is processed
- **THEN** an error is printed and no ticket is created

#### Scenario: Unknown user rejected
- **WHEN** `TICKET CREATE;T003;nobody@correo.com;2027-04-15` is processed
- **THEN** an error is printed and no ticket is created

### Requirement: Delete individual ticket
`TICKET DELETE;<identifier>` SHALL delete a stand-alone individual ticket whose date is strictly after the current system date. Tickets dated today or earlier, individual tickets belonging to a group ticket, group-ticket identifiers and unknown identifiers SHALL be rejected.

#### Scenario: Delete future ticket
- **WHEN** T001 is dated 2027-04-16, the system date is 2027-04-15 and `TICKET DELETE;T001` is processed
- **THEN** T001 no longer exists

#### Scenario: Ticket for today kept
- **WHEN** T001 is dated today and `TICKET DELETE;T001` is processed
- **THEN** an error is printed and T001 still exists

#### Scenario: Composition ticket cannot be deleted alone
- **WHEN** `TICKET DELETE;TG01:12345678Z` is processed for a ticket inside group ticket TG01
- **THEN** an error is printed and TG01 is unchanged

### Requirement: Create group ticket
`GROUPTICKET CREATE;<identifier>;<group>;<date>` SHALL, for an existing non-empty group and a date on or after today, create a group ticket composed of one individual ticket per current member, identified `<identifier>:<dni>`, and print the total price. The composition SHALL NOT change afterwards.

#### Scenario: Create group ticket
- **WHEN** G01 has Ana (DNI 12345678Z) and Pedro and `GROUPTICKET CREATE;TG01;G01;2027-04-15` is processed
- **THEN** TG01 is created with tickets `TG01:12345678Z` and `TG01:87654321X` and a total of 80.00 euros is displayed

#### Scenario: Empty group rejected
- **WHEN** the group has no members
- **THEN** an error is printed and no ticket is created

#### Scenario: Derived identifier conflict
- **WHEN** a ticket `TG01:12345678Z` already exists and `GROUPTICKET CREATE;TG01;G01;2027-04-15` is processed for a group containing that DNI
- **THEN** an error is printed and no part of TG01 is created

#### Scenario: Composition fixed
- **WHEN** TG01 has become historical and a member is later removed from G01
- **THEN** TG01 still contains the removed member's ticket

### Requirement: Group discount
Each individual ticket in a group ticket SHALL cost the current base price minus a discount by member count: fewer than 3 → 0%, 3 to 5 → 10%, 6 to 10 → 20%, more than 10 → 50%. The group ticket total SHALL be the sum of its tickets.

#### Scenario: Six members
- **WHEN** the base price is 40 and a 6-member group buys a group ticket
- **THEN** each ticket costs 32.00 and the displayed total is 192.00 euros

#### Scenario: Bracket limits
- **WHEN** group tickets are bought by groups of 2, 3, 5, 10 and 11 members at base price 40
- **THEN** the unit prices are 40.00, 36.00, 36.00, 32.00 and 20.00

### Requirement: Delete group ticket
`GROUPTICKET DELETE;<identifier>` SHALL delete a group ticket dated strictly after the current system date together with its composition. Group tickets dated today or earlier SHALL be kept and the command rejected.

#### Scenario: Delete future group ticket
- **WHEN** TG01 is dated after today and `GROUPTICKET DELETE;TG01` is processed
- **THEN** TG01 and all `TG01:<dni>` tickets no longer exist

#### Scenario: Historical group ticket kept
- **WHEN** TG01 is dated today or earlier and `GROUPTICKET DELETE;TG01` is processed
- **THEN** an error is printed and TG01 still exists

### Requirement: Active and historical tickets
A ticket SHALL be active when its date is on or after the current system date and historical otherwise; this state SHALL be derived, not stored.

#### Scenario: Ticket becomes historical
- **WHEN** a ticket dated 2027-04-15 exists and the system date is set to 2027-04-16
- **THEN** the ticket is historical
