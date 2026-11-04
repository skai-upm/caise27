# Spec Delta

## Purpose

Lets a group buy a joint ticket for a date that is composed of one individual ticket per member at purchase time, priced with a member-count discount, and that protects the group and its members while it is current or future.

## ADDED Requirements

### Requirement: Group ticket purchase
`GROUPTICKET CREATE;IdTicket;IdGrupo;yyyy-MM-dd` SHALL create a group ticket for an existing group with at least one member, with an identifier that is nonblank and unused by any ticket, and a valid date on or after the current system date. It SHALL create one constituent individual ticket per member at that moment, each referencing its user and priced at the current individual price. It SHALL reply exactly `OK GROUP TICKET CREATED -> <Precio>`, with the total shown with two decimals.

#### Scenario: Purchase for a group
- **GIVEN** system date 2026-10-01, price 40.00 and group `G01` with three members
- **WHEN** `GROUPTICKET CREATE;TG01;G01;2026-10-15` is processed
- **THEN** the reply is `OK GROUP TICKET CREATED -> 108.00` and exactly three constituent tickets dated 2026-10-15 exist, one per member

#### Scenario: Rejected purchases
- **WHEN** the group does not exist, the group has no members, the identifier is blank or already used by an individual, group or constituent ticket, the date is malformed or before the system date, or the parameter count is wrong
- **THEN** an `ERROR` message is produced and no group or constituent ticket is created

### Requirement: Group discount brackets
The group ticket total SHALL be the individual price multiplied by the number of members, minus a discount of 0% for fewer than 3 members, 10% for 3 to 5, 20% for 6 to 10 and 50% for more than 10, rounded to two decimals.

#### Scenario: Bracket boundaries at price 40.00
- **WHEN** group tickets are bought for groups of 1, 2, 3, 5, 6, 10 and 11 members
- **THEN** the totals are 40.00, 80.00, 108.00, 180.00, 192.00, 320.00 and 220.00

#### Scenario: Price change applies to later purchases
- **GIVEN** `SYSTEM PRICE;60.00`
- **WHEN** a group of 3 buys a group ticket
- **THEN** the total is 162.00 and each constituent ticket has price 60.00

### Requirement: Composition fixed at purchase
A group ticket's constituent tickets SHALL be those of the members at purchase time, and later membership changes SHALL NOT change them.

#### Scenario: Member added after a past ticket
- **GIVEN** group `G01` with Ana and Pedro bought `TG01` dated 2026-10-15
- **WHEN** the system date is 2026-10-16 and Luis is added to `G01`
- **THEN** `TG01` still has exactly the constituent tickets of Ana and Pedro

### Requirement: Group lock
While a group has any group ticket dated on or after the current system date, `GROUP ADD`, `GROUP REMOVE` and `GROUP DELETE` on that group SHALL be rejected. When all its group tickets are dated before the system date, these operations SHALL be allowed again.

#### Scenario: Locked group
- **GIVEN** `TG01` for `G01` dated 2026-10-15 and system date 2026-10-01 (or 2026-10-15)
- **WHEN** a member is added or removed, or `GROUP DELETE;G01` is processed
- **THEN** an `ERROR` message is produced and the group is unchanged

#### Scenario: Unlocked when tickets are past
- **GIVEN** `TG01` for `G01` dated 2026-10-15
- **WHEN** `SYSTEM DATE;2026-10-16` is processed and a member is removed and the group is deleted
- **THEN** both operations succeed and `TG01` is kept as history

### Requirement: Constituent tickets protect members
A user SHALL NOT be deleted while any constituent ticket referencing that user is dated on or after the current system date.

#### Scenario: Member with an active group ticket
- **GIVEN** Ana is a member covered by `TG01` dated 2026-10-15 and the system date is 2026-10-01
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** an `ERROR` message is produced and Ana is not deleted; after `SYSTEM DATE;2026-10-16` the deletion succeeds

### Requirement: Group ticket deletion
`GROUPTICKET DELETE;IdTicket` SHALL delete the group ticket together with all its constituent tickets only when its date is strictly later than the current system date; a group ticket dated on or before the system date, or an unknown identifier, SHALL be rejected.

#### Scenario: Future group ticket
- **GIVEN** `TG001` dated 2026-10-15 and system date 2026-10-01
- **WHEN** `GROUPTICKET DELETE;TG001` is processed
- **THEN** an `OK` message is produced, the group ticket and its constituent tickets no longer exist, and the group is unlocked

#### Scenario: Current, past or unknown group ticket
- **WHEN** the system date is on or after the ticket date, or the identifier does not exist
- **THEN** an `ERROR` message is produced and nothing is deleted
