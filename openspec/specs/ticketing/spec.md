# ticketing Specification

## Purpose

Defines individual and group tickets: their shared identifier space, creation, pricing and discounts, deletion rules and retention as historical records.

## Requirements

### Requirement: Common ticket abstraction
Every ticket SHALL have a unique identifier, a date of use and price information. Individual and group tickets MUST share a common abstraction that reuses common data and price behaviour, and operations that do not need the concrete type MUST work through that abstraction.

#### Scenario: Shared identifier space
- **WHEN** `TICKET CREATE;T001;...` exists and `GROUPTICKET CREATE;T001;G01;...` is processed
- **THEN** the group ticket is rejected because T001 is already used

### Requirement: Active and historical tickets
A ticket SHALL be active when its date is equal to or later than the current system date, and historical otherwise.

#### Scenario: Ticket becomes historical
- **WHEN** a ticket dated 2027-04-15 exists and the date is set to 2027-04-16
- **THEN** the ticket is historical

### Requirement: Individual ticket creation
`TICKET CREATE;<identifier>;<email>;<date>` SHALL create a ticket for an existing user, valid only on the given date, priced at the current base price, and display that price. It MUST reject a used or empty identifier, an unknown user, an invalid date and a date earlier than the current system date. The ticket references its user; the user does not reference tickets.

#### Scenario: Create ticket
- **WHEN** the date is 2027-04-15 and `TICKET CREATE;T001;ana@correo.com;2027-04-15` is processed
- **THEN** T001 is created for Ana and the price 40.00 is displayed

#### Scenario: Past date rejected
- **WHEN** the date is 2027-04-15 and `TICKET CREATE;T002;ana@correo.com;2027-04-14` is processed
- **THEN** an error is shown and no ticket is created

#### Scenario: Unknown user rejected
- **WHEN** `TICKET CREATE;T003;nadie@correo.com;2027-04-15` is processed
- **THEN** an error is shown and no ticket is created

### Requirement: Individual ticket deletion
`TICKET DELETE;<identifier>` SHALL delete an individual ticket only when its date is strictly later than the current system date. Tickets for the current day or earlier MUST be kept as historical records, and individual tickets that belong to a group ticket MUST NOT be deleted with this command.

#### Scenario: Delete future ticket
- **WHEN** the date is 2027-04-15 and `TICKET DELETE;T005` is processed for a ticket dated 2027-04-20
- **THEN** T005 is deleted

#### Scenario: Today's ticket not deletable
- **WHEN** the date is 2027-04-15 and `TICKET DELETE;T001` is processed for a ticket dated 2027-04-15
- **THEN** an error is shown and T001 remains

#### Scenario: Composition ticket not deletable
- **WHEN** `TICKET DELETE;TG01:12345678Z` is processed
- **THEN** an error is shown and the group ticket is unchanged

### Requirement: Group ticket creation
`GROUPTICKET CREATE;<identifier>;<group>;<date>` SHALL create, for an existing non-empty group and a date not earlier than the current system date, a group ticket composed of one individual ticket per current member with identifier `<groupTicketIdentifier>:<dni>`, and display the total price. The group ticket identifier and every derived identifier MUST be free; otherwise nothing is created.

#### Scenario: Create group ticket
- **WHEN** G01 has Ana (12345678Z) and Pedro (87654321X) and `GROUPTICKET CREATE;TG01;G01;2027-04-15` is processed
- **THEN** TG01 is created with individual tickets `TG01:12345678Z` and `TG01:87654321X` and the total 80.00 is displayed

#### Scenario: Empty group rejected
- **WHEN** GROUPTICKET CREATE is processed for a group with no members
- **THEN** an error is shown and no ticket is created

#### Scenario: Derived identifier conflict
- **WHEN** an individual ticket `TG01:12345678Z` already exists and `GROUPTICKET CREATE;TG01;G01;2027-04-15` is processed
- **THEN** an error is shown and no part of TG01 is created

#### Scenario: Composition fixed at purchase
- **WHEN** TG01 has been created and later the group's membership changes
- **THEN** TG01's composition is unchanged

### Requirement: Group discount
The discount applied to the base price of each individual ticket in a group ticket SHALL depend on the number of members included: fewer than 3 → 0 %, 3 to 5 → 10 %, 6 to 10 → 20 %, more than 10 → 50 %. The group total MUST be the sum of its individual ticket prices.

#### Scenario: Six members at 40 euros
- **WHEN** the base price is 40 and a 6-member group buys a group ticket
- **THEN** each individual ticket costs 32.00 and the displayed total is 192.00

#### Scenario: Bracket limits
- **WHEN** group tickets are bought for 2, 3, 5, 6, 10 and 11 members
- **THEN** the applied discounts are 0 %, 10 %, 10 %, 20 %, 20 % and 50 %

### Requirement: Group ticket deletion
`GROUPTICKET DELETE;<identifier>` SHALL delete a group ticket and all the individual tickets in its composition only when its date is strictly later than the current system date.

#### Scenario: Delete future group ticket
- **WHEN** TG01 is dated after the current date and `GROUPTICKET DELETE;TG01` is processed
- **THEN** TG01 and its composition tickets are deleted

#### Scenario: Today's group ticket not deletable
- **WHEN** TG01 is dated on the current date
- **THEN** `GROUPTICKET DELETE;TG01` is rejected
