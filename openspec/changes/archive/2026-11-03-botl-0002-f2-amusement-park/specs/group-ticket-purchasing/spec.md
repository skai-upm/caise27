# Spec Delta

## Purpose

Lets a non-empty group buy one discounted, atomically created group ticket composed of an individual ticket per member, and delete it while it is still in the future (source F2-C04, sections 7.3 and 8).

## ADDED Requirements

### Requirement: Create group ticket
`GROUPTICKET CREATE;<identifier>;<group>;<date>` SHALL create, for an existing non-empty group and a date not earlier than the current system date, a group ticket composed of one new individual ticket per current member, referencing the group, and SHALL display the total price.

#### Scenario: Group ticket created
- **WHEN** group `G01` has Ana (DNI `12345678Z`) and Pedro (DNI `87654321X`) and `GROUPTICKET CREATE;TG01;G01;2027-04-15` is processed on 2027-04-15
- **THEN** group ticket `TG01` exists with individual tickets `TG01:12345678Z` and `TG01:87654321X` and total price 80.00 is displayed

#### Scenario: Empty group rejected
- **WHEN** `GROUPTICKET CREATE;TG02;G02;2027-04-15` is processed for a group with no members
- **THEN** an error is reported and no ticket is created

#### Scenario: Past date or unknown group rejected
- **WHEN** the date is before the current system date or the group does not exist
- **THEN** an error is reported and no ticket is created

### Requirement: Derived identifiers and atomic creation
Each composed ticket identifier SHALL be `<groupTicketIdentifier>:<dni>`; the group ticket identifier and every derived identifier SHALL be checked against the shared identifier space before anything is created, and any conflict SHALL reject the whole purchase.

#### Scenario: Derived identifier collision
- **WHEN** a standalone ticket `TG03:12345678Z` exists and `GROUPTICKET CREATE;TG03;G01;<date>` is processed for a group containing DNI `12345678Z`
- **THEN** an error is reported and neither `TG03` nor any of its individual tickets is created

### Requirement: Group discount brackets
Each composed individual ticket SHALL cost the current base price minus a discount determined by the number of members included: fewer than 3 → 0 %, 3 to 5 → 10 %, 6 to 10 → 20 %, more than 10 → 50 %; the group ticket total SHALL be the sum of its individual ticket prices.

#### Scenario: Totals by group size at 40 euros
- **WHEN** groups of 2, 3, 6 and 11 members buy group tickets with base price 40.00
- **THEN** the totals are 80.00, 108.00, 192.00 and 220.00 respectively

### Requirement: Fixed composition and unidirectional references
A group ticket's composition SHALL NOT change after purchase even if the group changes; the group ticket SHALL reference its group while the group exists (the reference may become empty after group deletion) and SHALL NOT keep a separate user collection; groups SHALL NOT reference their tickets.

#### Scenario: Composition unchanged after membership change
- **WHEN** a member is removed from group `G01` after its group ticket `TG01` has become historical
- **THEN** `TG01` still contains the individual ticket of the removed member

### Requirement: Delete group ticket
`GROUPTICKET DELETE;<identifier>` SHALL delete an existing group ticket together with its composed individual tickets only when its date is strictly later than the current system date.

#### Scenario: Future group ticket deleted with its composition
- **WHEN** group ticket `TG01` is dated after the current date and `GROUPTICKET DELETE;TG01` is processed
- **THEN** `TG01` and all `TG01:<dni>` tickets no longer exist

#### Scenario: Group ticket for today retained
- **WHEN** group ticket `TG01` is dated today and `GROUPTICKET DELETE;TG01` is processed
- **THEN** an error is reported and the group ticket remains
