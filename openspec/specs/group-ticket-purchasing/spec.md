# group-ticket-purchasing Specification

## Purpose

Lets a nonempty group buy one discounted joint ticket composed of an individual ticket per current member, created atomically and deletable only while in the future.

## Requirements

### Requirement: Group ticket creation
`GROUPTICKET CREATE;<identifier>;<group>;<date>` SHALL create a group ticket for an existing group with at least one member, dated on or after the current system date, with a unique identifier. The ticket SHALL be composed of one new individual ticket per member at purchase time, identified as `<groupTicketIdentifier>:<dni>`, each referencing its member. The group ticket SHALL reference the purchasing group and SHALL NOT keep a separate user collection.

#### Scenario: Composition identifiers
- **WHEN** group G01 with Ana (DNI 12345678Z) and Pedro (DNI 87654321X) purchases `GROUPTICKET CREATE;TG01;G01;2027-04-15`
- **THEN** group ticket TG01 exists, composed of `TG01:12345678Z` for Ana and `TG01:87654321X` for Pedro

#### Scenario: Empty group, unknown group or past date
- **WHEN** `GROUPTICKET CREATE` names an empty group, a nonexistent group or a date before the current system date
- **THEN** creation is rejected and no ticket is created

### Requirement: Atomic group ticket creation
The group ticket identifier and every derived composition identifier SHALL be checked against the shared identifier space before anything is created; any conflict SHALL reject the whole purchase.

#### Scenario: Derived identifier collision
- **WHEN** a ticket `TG05:12345678Z` already exists and G01, containing Ana with DNI 12345678Z, purchases `GROUPTICKET CREATE;TG05;G01;2027-04-15`
- **THEN** creation is rejected and neither TG05 nor any of its composition tickets exists

### Requirement: Group discount pricing
Each composition ticket SHALL cost the current base price minus a discount determined by the number of members included: fewer than 3 members 0%, 3 to 5 members 10%, 6 to 10 members 20%, more than 10 members 50%. The group ticket total SHALL be the sum of its composition ticket prices, and the application SHALL display it.

#### Scenario: Discount brackets at 40 euros
- **WHEN** groups of 2, 3, 6 and 11 members each purchase a group ticket with base price 40.00
- **THEN** the displayed totals are 80.00, 108.00, 192.00 and 220.00 respectively

### Requirement: Fixed composition
The composition created at purchase time SHALL NOT change afterwards, even if the group's membership changes or the group is deleted.

#### Scenario: Later membership change
- **WHEN** the group ticket becomes historical and a member is then removed from the group
- **THEN** the group ticket's composition still contains that member's ticket

### Requirement: Group ticket deletion
`GROUPTICKET DELETE;<identifier>` SHALL delete a group ticket only when its date is strictly later than the current system date, together with all its composition tickets. Group tickets for today or earlier SHALL be retained as historical records.

#### Scenario: Future group ticket deleted
- **WHEN** TG02 is dated after today and `GROUPTICKET DELETE;TG02` is processed
- **THEN** TG02 and all its composition tickets no longer exist

#### Scenario: Group ticket for today kept
- **WHEN** TG01 is dated today and `GROUPTICKET DELETE;TG01` is processed
- **THEN** deletion is rejected and TG01 still exists
