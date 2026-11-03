# group-tickets Specification

## Purpose

Lets a group buy tickets jointly for a date, with a size-based discount, composed of one individual ticket per member at purchase time.

## Requirements

### Requirement: Group ticket purchase
The command `GROUPTICKET CREATE;Id;IdGrupo;yyyy-MM-dd` SHALL create a group ticket when the identifier is non-blank and not used by any ticket, the group exists and has at least one member, and the date is a valid `yyyy-MM-dd` date on or after the system date. On success it SHALL respond exactly `OK GROUP TICKET CREATED -> <Precio>` with the total price formatted with two decimals.

#### Scenario: Successful purchase
- **GIVEN** system date 2026-10-01, price 40.00 and group `G01` with 3 members
- **WHEN** `GROUPTICKET CREATE;TG01;G01;2026-10-15` is processed
- **THEN** the response is `OK GROUP TICKET CREATED -> 108.00`

#### Scenario: Rejected purchases
- **WHEN** the group does not exist, the group has no members, the date is before the system date or invalid, the identifier is already used by an individual or group ticket, or a parameter is missing
- **THEN** an error is reported and no ticket is created

### Requirement: One individual ticket per member
A group ticket SHALL be composed of exactly one individual ticket for each user belonging to the group at purchase time, each referring to that registered user, dated with the group ticket's date and priced at the member's discounted share: the individual price used for the purchase reduced by the group's discount percentage, rounded to two decimals. Later membership changes SHALL NOT alter the composition.

#### Scenario: Composition matches members at purchase
- **GIVEN** group `G01` with Ana and Pedro
- **WHEN** `GROUPTICKET CREATE;TG01;G01;2026-10-15` is processed
- **THEN** `TG01` contains two individual tickets, one for Ana and one for Pedro

#### Scenario: Composition frozen
- **GIVEN** `TG01` bought for `G01` with Ana and Pedro, now dated in the past
- **WHEN** Luis is added to `G01`
- **THEN** `TG01` still contains only the tickets of Ana and Pedro

#### Scenario: Discounted share stored in constituent tickets
- **GIVEN** an individual price of 40.00
- **WHEN** a group of 3 members buys `TG01`
- **THEN** each constituent ticket stores 36.00 and the group ticket total is 108.00

### Requirement: Size-based discount
The total price SHALL be the individual price multiplied by the number of members, reduced by 0% for fewer than 3 members, 10% for 3 to 5, 20% for 6 to 10 and 50% for more than 10.

#### Scenario: Discount brackets and boundaries
- **GIVEN** an individual price of 40.00
- **WHEN** groups of 2, 3, 5, 6, 10 and 11 members buy a group ticket
- **THEN** the totals are 80.00, 108.00, 180.00, 192.00, 320.00 and 220.00

### Requirement: Group ticket deletion
The command `GROUPTICKET DELETE;Id` SHALL delete the group ticket together with all its constituent individual tickets only if its date is strictly later than the system date; otherwise, or if it does not exist, it SHALL be rejected.

#### Scenario: Future group ticket
- **GIVEN** system date 2026-10-01 and `TG001` dated 2026-10-15
- **WHEN** `GROUPTICKET DELETE;TG001` is processed
- **THEN** `TG001` and its constituent tickets no longer exist

#### Scenario: Current or past group ticket
- **GIVEN** `TG001` dated 2026-10-15 and system date 2026-10-15 or later
- **WHEN** `GROUPTICKET DELETE;TG001` is processed
- **THEN** an error is reported and the group ticket is kept

### Requirement: Constituent tickets block user deletion
A constituent individual ticket dated on or after the system date SHALL prevent the deletion of its user, exactly as an ordinary individual ticket does; constituent tickets SHALL NOT be deletable on their own with `TICKET DELETE`.

#### Scenario: Member with valid group ticket
- **GIVEN** Ana belongs to `TG01` dated 2026-10-15 and the system date is 2026-10-01
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** an error is reported and Ana is not deleted

#### Scenario: Restriction ceases
- **GIVEN** the same ticket and `SYSTEM DATE;2026-10-16`
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** Ana is deleted and `TG01` is kept as history
