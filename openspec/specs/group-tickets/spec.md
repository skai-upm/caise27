# group-tickets Specification

## Purpose

Lets a non-empty group of Amusement Park users buy one joint ticket composed of an individual ticket per member, with a size-based discount, and governs how such tickets are deleted or kept as historical records.

## Requirements

### Requirement: Create group ticket
`GROUPTICKET CREATE;<identifier>;<group>;<date>` SHALL create a group ticket for an existing group with at least one member, dated on or after the current system date, composed of one individual ticket per member at purchase time, each referencing its user, and print the total price charged. It MUST be rejected with an error, creating nothing, when the identifier is used, the group does not exist or is empty, or the date is invalid or in the past.

#### Scenario: Composition created for every member
- **GIVEN** group `G01` with members of DNI `12345678Z` and `87654321X`
- **WHEN** `GROUPTICKET CREATE;TG01;G01;2027-04-15` is processed
- **THEN** group ticket `TG01` exists with individual tickets `TG01:12345678Z` and `TG01:87654321X`, each for its user and dated `2027-04-15`

#### Scenario: Empty group rejected
- **GIVEN** group `G02` with no members
- **WHEN** `GROUPTICKET CREATE;TG02;G02;2027-04-15` is processed
- **THEN** an error reporting an empty group is printed and no ticket is created

#### Scenario: Past date or unknown group rejected
- **WHEN** a group ticket is requested for a date before the current system date, or for group `G99` which does not exist
- **THEN** an error is printed and no ticket is created

### Requirement: Derived identifiers checked before creation
Each constituent ticket identifier SHALL be `<groupTicketIdentifier>:<dni>` and belongs to the shared ticket identifier space; every derived identifier MUST be checked before creation completes, and any conflict MUST leave no part of the group ticket created.

#### Scenario: Conflict in a constituent identifier
- **GIVEN** an individual ticket `TG01:12345678Z` already exists and `G01` contains the user with DNI `12345678Z`
- **WHEN** `GROUPTICKET CREATE;TG01;G01;2027-04-15` is processed
- **THEN** an error reporting the identifier conflict is printed and neither `TG01` nor any `TG01:<dni>` ticket is created

#### Scenario: Constituent identifiers block other tickets
- **GIVEN** group ticket `TG01` with constituent `TG01:12345678Z`
- **WHEN** `TICKET CREATE;TG01:12345678Z;ana@correo.com;2027-04-20` is processed
- **THEN** an error reporting that the ticket already exists is printed

### Requirement: Group discount
The discount SHALL depend on the number of members in the purchase: fewer than 3 → 0%, 3 to 5 → 10%, 6 to 10 → 20%, more than 10 → 50%, applied to the current base price of each constituent ticket; the group ticket total MUST be the sum of its constituent ticket prices.

#### Scenario: Six members at 40 euros
- **GIVEN** the base price is `40.00` and group `G02` has 6 members
- **WHEN** `GROUPTICKET CREATE;TG02;G02;2027-04-15` is processed
- **THEN** each constituent ticket costs `32.00` and the printed total is `192.00`

#### Scenario: Bracket boundaries
- **WHEN** group tickets are bought for 2, 3, 5, 10 and 11 members at base price `40.00`
- **THEN** each constituent ticket costs `40.00`, `36.00`, `36.00`, `32.00` and `20.00` respectively

### Requirement: Fixed composition and group reference
The composition created at purchase time MUST NOT change afterwards, even if the group's membership changes later. The group ticket SHALL reference its purchasing group while the group exists and MUST NOT keep a separate collection of users; neither the group nor users hold references to tickets.

#### Scenario: Later membership changes do not alter the ticket
- **GIVEN** group ticket `TG01` for `G01` is historical and `G01` then gains a new member
- **WHEN** `TG01` is inspected
- **THEN** its constituent tickets are exactly those created at purchase

### Requirement: Delete group ticket
`GROUPTICKET DELETE;<identifier>` SHALL delete the group ticket, together with its constituent tickets, only when its date is strictly later than the current system date; tickets for the current day or earlier MUST be kept as historical records and the command rejected with an error. Constituent tickets MUST NOT be deletable with `TICKET DELETE`.

#### Scenario: Future group ticket deleted and group unlocked
- **GIVEN** `G01`'s only active group ticket `TG01` is dated after the current date
- **WHEN** `GROUPTICKET DELETE;TG01` is processed
- **THEN** `TG01` and its constituent tickets no longer exist and `GROUP ADD` on `G01` is allowed again

#### Scenario: Group ticket for today kept
- **GIVEN** `TG01` is dated on the current system date
- **WHEN** `GROUPTICKET DELETE;TG01` is processed
- **THEN** an error is printed and `TG01` still exists

#### Scenario: Constituent cannot be deleted alone
- **WHEN** `TICKET DELETE;TG01:12345678Z` is processed
- **THEN** an error reporting that the ticket belongs to group ticket `TG01` is printed and nothing is deleted

### Requirement: Group tickets lock groups and users
While a group has a group ticket dated on or after the current system date, `GROUP ADD`, `GROUP REMOVE` and `GROUP DELETE` on it MUST be rejected; and `USER DELETE` MUST be rejected for any user with a constituent ticket of an active group ticket or belonging to that group.

#### Scenario: Group locked by an active group ticket
- **GIVEN** `G01` has group ticket `TG01` dated on or after the current date
- **WHEN** `GROUP ADD;G01;luis@correo.com` is processed
- **THEN** an error reporting the active group ticket is printed

#### Scenario: Member with a constituent ticket cannot be deleted
- **GIVEN** Ana has constituent ticket `TG01:12345678Z` of an active group ticket
- **WHEN** `USER DELETE;ana@correo.com;Clave1!` is processed
- **THEN** an error is printed and Ana still exists
