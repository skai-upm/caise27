# Spec Delta

## ADDED Requirements

### Requirement: Group ticket purchase
`GROUPTICKET CREATE;<identifier>;<group>;<date>` SHALL create a group ticket for an existing, non-empty group, for a date equal to or later than the current system date, with an identifier unique among all tickets. The group ticket SHALL be composed of one new individual ticket per user belonging to the group at that moment, each identified `<groupTicketIdentifier>:<dni>` and referencing its user, and SHALL reference the purchasing group. The application SHALL display the total price charged.

#### Scenario: Group ticket created
- **WHEN** group `G01` contains Ana (DNI `12345678Z`) and Pedro (DNI `87654321X`), the system date is `2027-04-01` and `GROUPTICKET CREATE;TG01;G01;2027-04-15` is processed
- **THEN** group ticket `TG01` for `G01` is created with individual tickets `TG01:12345678Z` and `TG01:87654321X`, and the total `80.00` is displayed

#### Scenario: Empty group rejected
- **WHEN** group `G02` has no members and `GROUPTICKET CREATE;TG02;G02;2027-04-15` is processed
- **THEN** an error is printed and no ticket is created

#### Scenario: Past date or unknown group rejected
- **WHEN** the system date is `2027-04-15` and `GROUPTICKET CREATE;TG03;G01;2027-04-14` or `GROUPTICKET CREATE;TG03;G99;2027-04-15` is processed
- **THEN** an error is printed and no ticket is created

#### Scenario: Derived identifier conflict prevents partial creation
- **WHEN** ticket `TG04:87654321X` already exists and `GROUPTICKET CREATE;TG04;G01;2027-04-15` is processed for a group containing Ana and Pedro
- **THEN** an error is printed and neither `TG04` nor `TG04:12345678Z` is created

#### Scenario: Composition fixed at purchase
- **WHEN** group ticket `TG01` was created for Ana and Pedro and later, once `TG01` is historical, Pedro is removed from `G01`
- **THEN** `TG01` is still composed of the tickets of Ana and Pedro

### Requirement: Group discount
Each individual ticket of a group ticket SHALL cost the current base price reduced by a discount that depends on the number of users included: fewer than 3 → 0%, 3 to 5 → 10%, 6 to 10 → 20%, more than 10 → 50%. The group ticket total SHALL be the sum of the prices of its individual tickets.

#### Scenario: Six members at 40 euros
- **WHEN** the base price is `40.00` and a group of 6 users buys `GROUPTICKET CREATE;TG02;G02;2027-04-15`
- **THEN** each of the six individual tickets costs `32.00` and the total displayed is `192.00`

#### Scenario: Bracket limits
- **WHEN** group tickets are bought at base price `40.00` by groups of 2, 3, 5, 10 and 11 users
- **THEN** the per-ticket prices are `40.00`, `36.00`, `36.00`, `32.00` and `20.00` respectively

### Requirement: Group ticket deletion
`GROUPTICKET DELETE;<identifier>` SHALL delete a group ticket only when its date is strictly later than the current system date, together with the individual tickets of its composition. Group tickets dated on the current day or earlier SHALL be kept as historical records. Individual tickets of a group ticket SHALL NOT be deleted with `TICKET DELETE`.

#### Scenario: Future group ticket deleted
- **WHEN** the system date is `2027-04-01`, `TG01` is dated `2027-04-15` and `GROUPTICKET DELETE;TG01` is processed
- **THEN** a confirmation is printed and `TG01`, `TG01:12345678Z` and `TG01:87654321X` no longer exist

#### Scenario: Group ticket for today kept
- **WHEN** the system date is `2027-04-15`, `TG01` is dated `2027-04-15` and `GROUPTICKET DELETE;TG01` is processed
- **THEN** an error is printed and `TG01` still exists

#### Scenario: Composed ticket not deletable alone
- **WHEN** `TICKET DELETE;TG01:12345678Z` is processed
- **THEN** an error is printed and the ticket still exists
