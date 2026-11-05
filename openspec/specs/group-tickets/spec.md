# group-tickets Specification

## Purpose
Defines joint tickets purchased by a group: their composition of individual tickets, derived identifiers, member-count discount, atomic creation, deletion and retention as historical records.

## Requirements

### Requirement: Group ticket purchase
`GROUPTICKET CREATE;<identifier>;<group>;<date>` SHALL create a group ticket for the existing, non-empty group, valid on the given `yyyy-MM-dd` date, composed of one new individual ticket per member at purchase time identified as `<identifier>:<dni>` and referencing that member, and SHALL print a response starting with `OK` that shows the total charged. The group ticket SHALL reference the purchasing group. The group MUST exist and contain at least one user, and the date MUST be valid and not earlier than the current system date; otherwise an `ERROR` response is printed and nothing is created.

#### Scenario: Composition created with derived identifiers
- **WHEN** group `G01` contains Ana (DNI `12345678Z`) and Pedro (DNI `87654321X`) and `GROUPTICKET CREATE;TG01;G01;2027-04-15` is processed
- **THEN** group ticket `TG01` exists for 2027-04-15 referencing `G01`
- **AND** individual tickets `TG01:12345678Z` for Ana and `TG01:87654321X` for Pedro exist and form its composition

#### Scenario: Empty or unknown group rejected
- **WHEN** `GROUPTICKET CREATE` is processed for an empty group or an unknown group
- **THEN** an `ERROR` response is printed and no ticket is created

#### Scenario: Past or invalid date rejected
- **WHEN** the date is before the current system date or is not a valid `yyyy-MM-dd` date
- **THEN** an `ERROR` response is printed and no ticket is created

#### Scenario: Composition does not follow later group changes
- **WHEN** a group ticket for `G01` exists and becomes historical, and a user is then added to `G01`
- **THEN** the group ticket's composition is unchanged

### Requirement: Atomic creation in the shared identifier space
The group ticket identifier and every derived `<identifier>:<dni>` identifier SHALL be checked against all existing tickets before anything is created; if any of them is already used, the command MUST be rejected with an `ERROR` response and no part of the group ticket is created.

#### Scenario: Derived identifier conflict
- **WHEN** an individual ticket `TG01:87654321X` already exists and `GROUPTICKET CREATE;TG01;G01;2027-04-15` is processed for a group containing Pedro (`87654321X`)
- **THEN** an `ERROR` response is printed and neither `TG01` nor any `TG01:<dni>` ticket for the other members is created

#### Scenario: Group ticket identifier conflict
- **WHEN** a ticket `TG01` already exists
- **THEN** `GROUPTICKET CREATE;TG01;G01;2027-04-15` is rejected and no ticket is created

### Requirement: Member-count discount
Each individual ticket in the composition SHALL cost the current system base price reduced by the discount for the number of members included: fewer than 3 → 0%, 3 to 5 → 10%, 6 to 10 → 20%, more than 10 → 50%. The group ticket total SHALL be the sum of its individual ticket prices and SHALL NOT change when the system price later changes.

#### Scenario: Six members at 40 euros
- **WHEN** the base price is 40.00 and a group of 6 users buys a group ticket
- **THEN** each of the 6 individual tickets costs 32.00 and the response shows a total of 192.00

#### Scenario: Bracket boundaries
- **WHEN** groups of 2, 3, 5, 10 and 11 members buy group tickets at a base price of 40.00
- **THEN** the individual prices are 40.00, 36.00, 36.00, 32.00 and 20.00 respectively

### Requirement: Group ticket deletion
`GROUPTICKET DELETE;<identifier>` SHALL delete, as one consistent operation without partial deletions, all SpeedPasses and Special Passes associated with the individual tickets of its composition, the individual tickets of the composition, and the group ticket itself, and print a response starting with `OK` only when its date is strictly later than the current system date. Group tickets for the current day or earlier MUST NOT be deleted and are retained as historical records. An unknown identifier, or one that does not identify a group ticket, MUST be rejected with an `ERROR` response.

#### Scenario: Future group ticket deleted with its composition
- **WHEN** `TG01` is dated after the system date and `GROUPTICKET DELETE;TG01` is processed
- **THEN** `TG01` and every `TG01:<dni>` ticket no longer exist

#### Scenario: Today's group ticket kept
- **WHEN** `TG01` is dated on the current system date
- **THEN** `GROUPTICKET DELETE;TG01` is rejected and `TG01` and its composition still exist

#### Scenario: Composition ticket not deletable on its own
- **WHEN** `TICKET DELETE;TG01:12345678Z` is processed
- **THEN** an `ERROR` response is printed and the ticket still exists

#### Scenario: Passes of the composition deleted with the group ticket
- **GIVEN** `TG01` is dated after the system date and its composition ticket `TG01:12345678Z` has SpeedPass `SP05:1` and Special Pass `PE05`
- **WHEN** `GROUPTICKET DELETE;TG01` is processed
- **THEN** `SP05:1`, `PE05`, every `TG01:<dni>` ticket and `TG01` no longer exist

#### Scenario: Rejected group ticket deletion keeps the passes
- **GIVEN** `TG01` is dated on the current system date and `TG01:12345678Z` has SpeedPass `SP05:1`
- **WHEN** `GROUPTICKET DELETE;TG01` is processed
- **THEN** an `ERROR` response is printed and `TG01`, its composition and `SP05:1` still exist
