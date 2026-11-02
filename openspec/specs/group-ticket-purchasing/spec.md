# group-ticket-purchasing Specification

## Purpose

Lets a nonempty group buy one joint ticket composed of an individual ticket per current member, with a size-based discount, created and deleted atomically.

## Requirements

### Requirement: Group ticket creation
`GROUPTICKET CREATE;<identifier>;<group>;<date>` SHALL, for an existing nonempty group, a new identifier and a date not before the system date, create a group ticket composed of one individual ticket per current member with identifier `<groupTicketIdentifier>:<dni>` referencing that member, and SHALL display the total price. The group ticket SHALL reference the purchasing group while it exists; the group SHALL NOT reference its tickets. (F2-C04)

#### Scenario: Composition and identifiers
- **WHEN** G01 contains Ana (DNI 12345678Z) and Pedro (DNI 87654321X) and `GROUPTICKET CREATE;TG01;G01;<today>` runs
- **THEN** TG01 exists with individual tickets `TG01:12345678Z` and `TG01:87654321X` for Ana and Pedro

#### Scenario: Empty group or past date
- **WHEN** GROUPTICKET CREATE targets an empty group, a nonexistent group, or a date before today
- **THEN** no ticket is created

#### Scenario: Composition fixed at purchase
- **WHEN** a group's membership changes after its group ticket has become historical
- **THEN** that group ticket's composition is unchanged

### Requirement: Group discount brackets
Each constituent ticket SHALL be priced at the current base price minus a discount determined by the number of members in the purchase: fewer than 3 → 0%, 3 to 5 → 10%, 6 to 10 → 20%, more than 10 → 50%; the group ticket total SHALL be the sum of its constituent ticket prices. (F2-C04)

#### Scenario: Totals at 40 euros
- **WHEN** groups of 2, 3, 6 and 11 members buy group tickets at base price 40.00
- **THEN** the totals are 80.00, 108.00, 192.00 and 220.00 euros respectively

### Requirement: Atomic identifier check
Before a group ticket is created, its identifier and every derived constituent identifier SHALL be checked against the shared ticket identifier space; on any conflict neither the group ticket nor any constituent ticket SHALL be created. (F2-C04)

#### Scenario: Derived identifier collision
- **WHEN** an individual ticket `TG01:12345678Z` already exists and G01 (containing DNI 12345678Z) runs `GROUPTICKET CREATE;TG01;G01;<today>`
- **THEN** the purchase is rejected and no ticket TG01 or other constituent ticket exists

### Requirement: Group ticket deletion
`GROUPTICKET DELETE;<identifier>` SHALL delete a group ticket only when its date is strictly later than the system date, deleting its constituent individual tickets with it; group tickets for today or earlier SHALL be retained as historical records. (F2-C04)

#### Scenario: Future group ticket deleted with composition
- **WHEN** `GROUPTICKET DELETE;TG01` targets a group ticket dated after today
- **THEN** TG01 and all `TG01:<dni>` tickets no longer exist

#### Scenario: Today's group ticket kept
- **WHEN** `GROUPTICKET DELETE` targets a group ticket dated today
- **THEN** deletion is rejected
