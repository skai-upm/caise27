# tickets Specification

## Purpose

Defines park tickets: individual tickets for one user and group tickets composed of one individual ticket per group member, their shared identifier space, pricing with group discounts, and when they can be created or deleted.

## Requirements

### Requirement: Shared ticket identifier space
Every ticket (individual, group, and individual tickets inside a group ticket) SHALL have an identifier unique among all tickets, compared ignoring case. Identifiers SHALL be non-empty.

#### Scenario: Identifier used by another ticket type
- **WHEN** group ticket TG01 exists and `TICKET CREATE;TG01;ana@correo.com;2027-04-15` is processed
- **THEN** an error that the identifier is already used is printed and no ticket is created

### Requirement: Active and historical tickets
A ticket SHALL be active when its date is on or after the current system date, and historical otherwise. A group ticket's member tickets share its date.

#### Scenario: Ticket becomes historical
- **WHEN** a ticket is dated 2027-04-15 and the system date becomes 2027-04-16
- **THEN** the ticket is historical

### Requirement: Individual ticket creation
`TICKET CREATE;<identifier>;<email>;<date>` SHALL create a ticket for an existing user, valid only on the given date, which SHALL be a valid `yyyy-MM-dd` date on or after the system date. Its price is the current base price, and the price charged SHALL be displayed.

#### Scenario: Create ticket
- **WHEN** the system date is 2027-04-15, the price is 40.00 and `TICKET CREATE;T001;ana@correo.com;2027-04-15` is processed
- **THEN** `TICKET CREATED` with a price of 40.00 EUR is printed

#### Scenario: Past date
- **WHEN** the system date is 2027-04-15 and `TICKET CREATE;T009;ana@correo.com;2027-04-14` is processed
- **THEN** an error is printed and no ticket is created

#### Scenario: Unknown user
- **WHEN** the email does not belong to an existing user
- **THEN** an error is printed and no ticket is created

### Requirement: Individual ticket deletion
`TICKET DELETE;<identifier>` SHALL delete an individual ticket only when its date is strictly after the system date, together with all SpeedPasses and the Special Pass bought for it. Tickets dated today or earlier SHALL be kept as historical records. Individual tickets that belong to a group ticket, and group tickets themselves, SHALL NOT be deleted with this command.

#### Scenario: Delete future ticket
- **WHEN** the system date is 2027-04-15 and `TICKET DELETE;T002` is processed for a ticket dated 2027-04-20
- **THEN** `TICKET DELETED` is printed and T002 no longer exists

#### Scenario: Ticket for today
- **WHEN** `TICKET DELETE;T001` is processed for a ticket dated on the system date
- **THEN** an error is printed and T001 still exists

#### Scenario: Member of a group ticket
- **WHEN** `TICKET DELETE;TG02:87654321X` is processed
- **THEN** an error stating that it belongs to a group ticket is printed and nothing is deleted

#### Scenario: Passes deleted with the ticket
- **WHEN** future ticket T002 has SpeedPasses from two purchases and a Special Pass and `TICKET DELETE;T002` succeeds
- **THEN** all those passes no longer exist

### Requirement: Group ticket creation
`GROUPTICKET CREATE;<identifier>;<group>;<date>` SHALL, for an existing non-empty group and a date on or after the system date, create a group ticket composed of one new individual ticket per current member, identified `<identifier>:<dni>`. The group ticket and all member identifiers SHALL be checked against the shared identifier space before anything is created; on any conflict nothing is created. Later changes to the group SHALL NOT change the composition.

#### Scenario: Create group ticket
- **WHEN** G01 has Ana (12345678Z), Pedro and Laura and `GROUPTICKET CREATE;TG01;G01;2027-04-15` is processed
- **THEN** `GROUP TICKET CREATED` with the total price is printed and tickets `TG01:12345678Z`, `TG01:87654321X` and `TG01:11111111H` exist inside TG01

#### Scenario: Empty group
- **WHEN** the group has no members
- **THEN** an error is printed and no ticket is created

#### Scenario: Derived identifier conflict
- **WHEN** an individual ticket `TG05:12345678Z` already exists and G01 (containing Ana) buys `TG05`
- **THEN** an error is printed and neither TG05 nor any of its member tickets is created

### Requirement: Group discounts
Each member ticket's price SHALL be the current base price minus a discount determined by the number of members at purchase: fewer than 3 → 0%, 3 to 5 → 10%, 6 to 10 → 20%, more than 10 → 50%. The group ticket's total SHALL be the sum of its member ticket prices, rounded to cents.

#### Scenario: Six members
- **WHEN** the base price is 40.00 and a group of 6 users buys a group ticket
- **THEN** each member ticket costs 32.00 EUR and the displayed total is 192.00 EUR

#### Scenario: Two members
- **WHEN** the base price is 45.50 and a group of 2 users buys a group ticket
- **THEN** the displayed total is 91.00 EUR

### Requirement: Group ticket deletion
`GROUPTICKET DELETE;<identifier>` SHALL delete a group ticket, together with all its member tickets and all passes bought for those member tickets, only when its date is strictly after the system date. If it cannot be deleted, nothing SHALL be deleted.

#### Scenario: Delete future group ticket
- **WHEN** the system date is 2027-04-15 and `GROUPTICKET DELETE;TG02` is processed for a group ticket dated 2027-04-20
- **THEN** `GROUP TICKET DELETED` is printed and TG02 and its member tickets no longer exist

#### Scenario: Group ticket for today
- **WHEN** `GROUPTICKET DELETE;TG01` is processed for a group ticket dated on the system date
- **THEN** an error is printed and TG01 still exists

#### Scenario: Member passes deleted
- **WHEN** a member ticket of future group ticket TG02 has a SpeedPass and TG02 is deleted
- **THEN** that SpeedPass no longer exists
