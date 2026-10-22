# group-tickets Specification

## Purpose

Lets a group buy one joint ticket composed of an individual ticket for each member, with a discount that depends on the number of members.

## Requirements

### Requirement: Group ticket purchase
`GROUPTICKET CREATE;<identifier>;<group>;<yyyy-MM-dd>` SHALL create a group ticket for an existing, non-empty group, dated today or later. It SHALL contain one individual ticket per current member, with identifier `<identifier>:<member DNI>`. It SHALL print `GROUPTICKET CREATED` with the total price. A missing or empty group, past or invalid date, or used identifier (including any derived one) SHALL be rejected, and nothing created.

#### Scenario: Purchase for three members
- **WHEN** group `G01` has Ana (`12345678Z`), Pedro, and Laura, the price is 40.00, and `GROUPTICKET CREATE;TG01;G01;2027-04-15` is processed on `2027-04-15`
- **THEN** group ticket `TG01` is created with three individual tickets including `TG01:12345678Z`, and the printed total is `108.00`

#### Scenario: Empty group rejected
- **WHEN** `GROUPTICKET CREATE;TG03;G03;2027-04-15` is processed and `G03` has no members
- **THEN** an error is printed and no ticket is created

#### Scenario: Derived identifier conflict
- **WHEN** a ticket `TG04:12345678Z` already exists and `GROUPTICKET CREATE;TG04;G01;2027-04-15` is processed for a group containing that DNI
- **THEN** an error is printed and neither `TG04` nor any of its individual tickets is created

### Requirement: Group discount brackets
Each individual ticket in a group ticket SHALL be priced at the current base price minus a discount based on the number of members included at purchase: fewer than 3 members, 0%; 3 to 5, 10%; 6 to 10, 20%; more than 10, 50%. Each discounted price SHALL be rounded half-up to cents. The group ticket's total SHALL be the sum of its individual tickets' prices.

#### Scenario: Six members at 40 EUR
- **WHEN** a group of 6 members buys a group ticket while the price is 40.00
- **THEN** each of its 6 individual tickets costs 32.00 and the printed total is `192.00`

#### Scenario: Two members pay full price
- **WHEN** a group of 2 members buys a group ticket while the price is 45.50
- **THEN** each individual ticket costs 45.50 and the total is `91.00`

#### Scenario: Eleven members
- **WHEN** a group of 11 members buys a group ticket while the price is 40.00
- **THEN** each individual ticket costs 20.00 and the total is `220.00`

### Requirement: Fixed composition
The individual tickets of a group ticket SHALL be fixed at purchase and SHALL NOT change when the group's membership later changes or the group is deleted. They SHALL belong to exactly one group ticket.

#### Scenario: Later membership change does not alter composition
- **WHEN** a group ticket dated in the past exists for `G01` and a member is later removed from `G01`
- **THEN** the group ticket still contains that member's individual ticket

### Requirement: Group ticket deletion
`GROUPTICKET DELETE;<identifier>` SHALL delete a group ticket only when its date is strictly after the current system date. It SHALL delete the group ticket with all its individual tickets, persist, and print `GROUPTICKET DELETED`. Group tickets dated today or earlier, unknown identifiers, and identifiers of other ticket kinds SHALL be rejected with an error.

#### Scenario: Delete future group ticket
- **WHEN** the system date is `2027-04-15` and `GROUPTICKET DELETE;TG02` is processed for a group ticket dated `2027-04-20`
- **THEN** `GROUPTICKET DELETED` is printed and neither `TG02` nor any `TG02:<dni>` ticket exists

#### Scenario: Today's group ticket kept
- **WHEN** `GROUPTICKET DELETE;TG01` is processed for a group ticket dated on the current system date
- **THEN** an error is printed and `TG01` still exists
