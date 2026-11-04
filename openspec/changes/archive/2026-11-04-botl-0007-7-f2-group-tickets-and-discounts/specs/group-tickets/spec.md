# Spec Delta

## Purpose

Lets a group buy a ticket for a date as a whole, composed of one individual ticket per member at purchase time, priced with member-count discounts, and locking the group while it is valid.

## ADDED Requirements

### Requirement: Group ticket composition
`GROUPTICKET CREATE;<Id>;<IdGrupo>;<yyyy-MM-dd>` SHALL create a group ticket for an existing group with at least one member, with an unused identifier and a valid date on or after the system date, composed of exactly one individual ticket per member at that moment, each referring to its user and priced at the current individual price; later membership changes SHALL NOT alter the composition.

#### Scenario: One ticket per member
- **GIVEN** system date `2026-10-01` and group `G01` with Ana, Pedro and Luis
- **WHEN** `GROUPTICKET CREATE;TG01;G01;2026-10-15` is processed
- **THEN** group ticket `TG01` exists with three individual tickets dated `2026-10-15`, one for each member

#### Scenario: Composition frozen at purchase
- **GIVEN** `TG01` was bought for `G01` with Ana and Pedro and its date has passed so the group is unlocked
- **WHEN** Luis is added to `G01`
- **THEN** `TG01` still contains only the tickets of Ana and Pedro

#### Scenario: Rejected purchases
- **WHEN** the group does not exist, the group has no members, the identifier is already used by any ticket, the date is before the system date or not a valid `yyyy-MM-dd` date, or a parameter is missing
- **THEN** the command is rejected and no group or individual ticket is created

### Requirement: Group ticket price and discount
The group ticket price SHALL be the sum of its individual tickets at the current individual price minus a discount by member count: fewer than 3 → 0%, 3 to 5 → 10%, 6 to 10 → 20%, more than 10 → 50%; on success the output SHALL be exactly `OK GROUP TICKET CREATED -> <Precio>` with two decimals.

#### Scenario: Discount brackets and boundaries at 40.00
- **GIVEN** the individual price is `40.00`
- **WHEN** group tickets are created for groups of 2, 3, 5, 6, 10 and 11 members
- **THEN** the outputs are `OK GROUP TICKET CREATED -> 80.00`, `108.00`, `180.00`, `192.00`, `320.00` and `220.00` respectively

#### Scenario: Single member
- **WHEN** a group with 1 member buys a group ticket at price `40.00`
- **THEN** the output is `OK GROUP TICKET CREATED -> 40.00`

### Requirement: Group ticket deletion
`GROUPTICKET DELETE;<Id>` SHALL delete the group ticket together with all its individual tickets only when its date is strictly later than the system date; otherwise, or for an unknown identifier, it SHALL be rejected.

#### Scenario: Future group ticket deleted
- **GIVEN** system date `2026-10-01` and `TG001` dated `2026-10-15`
- **WHEN** `GROUPTICKET DELETE;TG001` is processed
- **THEN** `TG001` and all its individual tickets are removed

#### Scenario: Current or past group ticket
- **GIVEN** `TG001` dated `2026-10-15`
- **WHEN** the system date is `2026-10-15` or later and `GROUPTICKET DELETE;TG001` is processed
- **THEN** it is rejected and nothing is removed

### Requirement: Common ticket abstraction
Individual tickets and group tickets SHALL share a common ticket abstraction exposing identifier, date and price, so operations that apply to any ticket can treat both uniformly.

#### Scenario: Uniform access
- **WHEN** all tickets are listed through the common abstraction
- **THEN** both individual and group tickets are included with their identifier, date and price
