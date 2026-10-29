# Spec Delta

## Purpose

Defines park tickets: a common abstraction with a shared identifier space, individual tickets for one user, and group tickets composed of discounted individual tickets, along with their creation, deletion and active/historical status.

## ADDED Requirements

### Requirement: Common ticket abstraction
Individual and group tickets SHALL share a common abstraction (class extension and/or interface implementation) that provides an identifier, a date of use and price behaviour, so that operations that do not need the concrete type handle both through that abstraction. Group tickets SHALL contain their individual tickets by composition.

#### Scenario: Handled uniformly
- **WHEN** an operation such as attraction entry receives either an individual or a group ticket identifier
- **THEN** it processes the ticket through the common abstraction

### Requirement: Shared identifier space
All tickets, including the derived identifiers of group-composition tickets, SHALL share one identifier space. No two tickets SHALL have the same identifier.

#### Scenario: Identifier reused across types
- **WHEN** `TICKET CREATE;TG01;...` is processed and a group ticket TG01 exists
- **THEN** an error is printed and no ticket is created

### Requirement: Active and historical tickets
A ticket SHALL be active when its date is on or after the current system date, and historical otherwise. Historical tickets SHALL be retained as records.

#### Scenario: Ticket becomes historical
- **WHEN** a ticket dated 2027-04-15 exists and the system date is set to 2027-04-16
- **THEN** the ticket is historical and is still kept

### Requirement: Individual ticket creation
`TICKET CREATE;<identifier>;<email>;<date>` SHALL create a ticket for an existing user with a new identifier, valid only on `<date>`, which SHALL be on or after the current system date. Its price SHALL be the current base price, and the application SHALL print the price charged. The ticket SHALL reference its user, and the user SHALL NOT reference its tickets.

#### Scenario: Create individual ticket
- **WHEN** the system date is 2027-04-01, the price is 40.00, and `TICKET CREATE;T001;ana@correo.com;2027-04-15` is processed
- **THEN** ticket T001 is created for Ana and a price of 40.00 is printed

#### Scenario: Past date rejected
- **WHEN** the system date is 2027-04-15 and `TICKET CREATE;T002;ana@correo.com;2027-04-14` is processed
- **THEN** an error is printed and no ticket is created

#### Scenario: Unknown user rejected
- **WHEN** `TICKET CREATE;T003;nadie@correo.com;2027-04-15` is processed
- **THEN** an error is printed and no ticket is created

### Requirement: Individual ticket deletion
`TICKET DELETE;<identifier>` SHALL delete an individual ticket only when its date is strictly after the current system date. It SHALL NOT delete tickets that belong to a group ticket's composition, or group tickets.

#### Scenario: Delete future ticket
- **WHEN** the system date is 2027-04-01 and `TICKET DELETE;T001` is processed for T001 dated 2027-04-15
- **THEN** T001 no longer exists

#### Scenario: Today's ticket kept
- **WHEN** the system date equals T001's date and `TICKET DELETE;T001` is processed
- **THEN** an error is printed and T001 still exists

#### Scenario: Composition ticket protected
- **WHEN** `TICKET DELETE;TG01:12345678Z` is processed
- **THEN** an error is printed and the ticket still exists

### Requirement: Group ticket creation
`GROUPTICKET CREATE;<identifier>;<group>;<date>` SHALL create, for an existing non-empty group and a date on or after the current system date, a group ticket composed of one individual ticket per current member with identifier `<identifier>:<dni>`, each referencing its user. All identifiers SHALL be checked first; on any conflict nothing SHALL be created. The group ticket SHALL reference its group, and the group SHALL NOT reference its tickets. The composition SHALL NOT change afterwards.

#### Scenario: Create group ticket
- **WHEN** G01 has members with DNIs 12345678Z and 87654321X and `GROUPTICKET CREATE;TG01;G01;2027-04-15` is processed
- **THEN** group ticket TG01 exists with composition tickets `TG01:12345678Z` and `TG01:87654321X`

#### Scenario: Empty group rejected
- **WHEN** `GROUPTICKET CREATE;TG01;G02;2027-04-15` is processed for an empty group G02
- **THEN** an error is printed and no ticket is created

#### Scenario: Derived identifier conflict
- **WHEN** a ticket `TG01:12345678Z` already exists and `GROUPTICKET CREATE;TG01;G01;2027-04-15` is processed with that member in G01
- **THEN** an error is printed and neither the group ticket nor any composition ticket is created

#### Scenario: Composition fixed
- **WHEN** a member is removed from G01 after TG01 has become historical
- **THEN** TG01's composition still contains that member's ticket

### Requirement: Group discount
Each composition ticket's price SHALL be the current base price minus a discount determined by the number of members in the purchase: fewer than 3 → 0 %, 3 to 5 → 10 %, 6 to 10 → 20 %, more than 10 → 50 %. The group ticket's total SHALL be the sum of its composition prices, and the application SHALL print it.

#### Scenario: Six members at 40 euros
- **WHEN** the price is 40.00 and a group of 6 users buys a group ticket
- **THEN** each composition ticket costs 32.00 and a total of 192.00 is printed

#### Scenario: Bracket boundaries
- **WHEN** group tickets are bought for 2, 3, 5, 10 and 11 members at 40.00
- **THEN** the unit prices are 40.00, 36.00, 36.00, 32.00 and 20.00 respectively

### Requirement: Group ticket deletion
`GROUPTICKET DELETE;<identifier>` SHALL delete a group ticket and its composition tickets only when its date is strictly after the current system date.

#### Scenario: Delete future group ticket
- **WHEN** the system date is before TG01's date and `GROUPTICKET DELETE;TG01` is processed
- **THEN** TG01 and all `TG01:<dni>` tickets no longer exist

#### Scenario: Today's group ticket kept
- **WHEN** the system date equals TG01's date and `GROUPTICKET DELETE;TG01` is processed
- **THEN** an error is printed and TG01 still exists
