# Spec Delta

## Purpose

Defines individual and group tickets for a date of use, their shared identifier space, pricing and group discounts, and the rules that keep current and past tickets as historical records.

## ADDED Requirements

### Requirement: Shared ticket identifier space
Ticket identifiers SHALL be unique across all ticket types, including the individual tickets that compose group tickets; any operation that would reuse an identifier SHALL be rejected without creating anything.

#### Scenario: Individual id used by group ticket
- **WHEN** group ticket `TG01` exists and `TICKET CREATE;TG01;ana@correo.com;2027-04-15` is processed
- **THEN** an error is printed and no ticket is created

#### Scenario: Derived id conflict
- **WHEN** an individual ticket `TG05:12345678Z` exists and `GROUPTICKET CREATE;TG05;G01;2027-04-20` is processed for a group containing the user with DNI 12345678Z
- **THEN** an error is printed and no part of TG05 is created

### Requirement: Active and historical tickets
A ticket SHALL be active when its date is on or after the current system date and historical otherwise.

#### Scenario: Ticket becomes historical
- **WHEN** a ticket is dated 2027-04-15 and the system date is set to 2027-04-16
- **THEN** the ticket is historical

### Requirement: Individual ticket creation
`TICKET CREATE;<identifier>;<email>;<date>` SHALL create a ticket for an existing user, valid only on the given date, which SHALL be on or after the current system date; its price SHALL be the current base price, and the price SHALL be printed on success.

#### Scenario: Create ticket
- **WHEN** the system date is 2027-04-15, the price 40.00, and `TICKET CREATE;T001;ana@correo.com;2027-04-15` is processed
- **THEN** ticket T001 exists for Ana and the output reports a price of 40.00

#### Scenario: Past date
- **WHEN** `TICKET CREATE;T009;ana@correo.com;2027-04-14` is processed with system date 2027-04-15
- **THEN** an error is printed and no ticket is created

#### Scenario: Unknown user
- **WHEN** `TICKET CREATE` names an email that is not registered
- **THEN** an error is printed

### Requirement: Individual ticket deletion
`TICKET DELETE;<identifier>` SHALL delete a standalone individual ticket only when its date is strictly after the current system date; tickets for today or earlier, group tickets and individual tickets that compose a group ticket SHALL NOT be deleted by this command.

#### Scenario: Delete future ticket
- **WHEN** T002 is dated 2027-04-20, the system date is 2027-04-15 and `TICKET DELETE;T002` is processed
- **THEN** T002 no longer exists and `TICKET DELETED` is printed

#### Scenario: Today's ticket
- **WHEN** `TICKET DELETE;T001` is processed for a ticket dated today
- **THEN** an error is printed and T001 is kept

#### Scenario: Group component
- **WHEN** `TICKET DELETE;TG01:12345678Z` is processed
- **THEN** an error is printed and the group ticket is unchanged

### Requirement: Group ticket creation
`GROUPTICKET CREATE;<identifier>;<group>;<date>` SHALL, for an existing non-empty group and a date on or after the current system date, create a group ticket referencing the group and composed of one individual ticket per current member identified `<identifier>:<dni>`; the composition SHALL NOT change afterwards, and the total price SHALL be printed on success.

#### Scenario: Create group ticket
- **WHEN** G01 has Ana (DNI 12345678Z), Pedro and Laura and `GROUPTICKET CREATE;TG01;G01;2027-04-15` is processed
- **THEN** TG01 contains tickets `TG01:12345678Z`, `TG01:87654321X`, `TG01:11111111H` and the total is printed

#### Scenario: Empty group
- **WHEN** `GROUPTICKET CREATE` is processed for a group with no members
- **THEN** an error is printed and no ticket is created

#### Scenario: Composition is fixed
- **WHEN** a member is removed from G01 after its group ticket became historical
- **THEN** the historical group ticket still contains that member's individual ticket

### Requirement: Group discount
Each individual ticket in a group ticket SHALL cost the current base price minus a discount determined by the number of members at purchase: fewer than 3 → 0%, 3 to 5 → 10%, 6 to 10 → 20%, more than 10 → 50%; the group ticket's total SHALL be the sum of its individual tickets.

#### Scenario: Six members
- **WHEN** the base price is 40.00 and a group of 6 creates a group ticket
- **THEN** each individual ticket costs 32.00 and the printed total is 192.00

#### Scenario: Three members
- **WHEN** the base price is 40.00 and a group of 3 creates a group ticket
- **THEN** the printed total is 108.00

### Requirement: Group ticket deletion
`GROUPTICKET DELETE;<identifier>` SHALL delete a group ticket and all the individual tickets composing it only when its date is strictly after the current system date.

#### Scenario: Delete future group ticket
- **WHEN** TG02 is dated after today and `GROUPTICKET DELETE;TG02` is processed
- **THEN** TG02 and its composing tickets no longer exist and `GROUP TICKET DELETED` is printed

#### Scenario: Today's group ticket
- **WHEN** `GROUPTICKET DELETE;TG01` is processed for a group ticket dated today
- **THEN** an error is printed and TG01 is kept
