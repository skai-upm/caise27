# group-tickets Specification

## Purpose
Lets a non-empty group buy a joint, discounted ticket composed of one individual ticket per member, and governs its deletion and its effect on the group.

## Requirements

### Requirement: GROUPTICKET CREATE command
The system SHALL accept `GROUPTICKET CREATE;<identifier>;<group>;<date>` and create a group ticket for the identified group valid on `<date>`. It SHALL reject with an error message, creating nothing, an unknown group, an empty group, a date earlier than the current system date, an invalid date, a wrong number of fields, or any identifier conflict.

#### Scenario: Create a group ticket
- **WHEN** group `G01` has members and `GROUPTICKET CREATE;TG01;G01;2027-04-15` is processed with system date 2027-04-01
- **THEN** group ticket `TG01` referencing group `G01` and dated 2027-04-15 is created and its total price is displayed

#### Scenario: Empty group rejected
- **WHEN** group `G03` has no members and `GROUPTICKET CREATE;TG03;G03;2027-04-15` is processed
- **THEN** an error message is displayed and no ticket is created

#### Scenario: Past date rejected
- **WHEN** the system date is 2027-04-15 and `GROUPTICKET CREATE;TG04;G01;2027-04-14` is processed
- **THEN** an error message is displayed and no ticket is created

### Requirement: Group ticket composition
A group ticket SHALL be composed of individual tickets, one per user belonging to the group at purchase time, each referencing its user and identified as `<groupTicketIdentifier>:<dni>`. The composition SHALL NOT change afterwards even if the group changes, SHALL NOT be shared with other group tickets, and the group ticket SHALL NOT keep a separate collection of users.

#### Scenario: Derived identifiers
- **WHEN** group ticket `TG01` is created for a group whose only member has DNI `12345678Z`
- **THEN** its composition contains the individual ticket `TG01:12345678Z` referencing that user

#### Scenario: Composition is fixed
- **WHEN** a group ticket's date becomes historical and a member is then removed from the group
- **THEN** the group ticket's composition still contains that member's individual ticket

### Requirement: Shared identifier space for group tickets
The group ticket identifier and every derived individual identifier SHALL be checked against all existing tickets before creation; on any conflict the group ticket SHALL NOT be partially created.

#### Scenario: Derived identifier conflict
- **WHEN** an individual ticket `TG01:12345678Z` already exists and `GROUPTICKET CREATE;TG01;G01;2027-04-15` is processed for a group containing the user with DNI `12345678Z`
- **THEN** an error message is displayed and neither `TG01` nor any of its derived tickets is created

### Requirement: Group discount pricing
Each individual ticket in a group ticket SHALL cost the current system base price minus a discount based on the number of members included: fewer than 3 → 0%, 3 to 5 → 10%, 6 to 10 → 20%, more than 10 → 50%. The total group ticket price SHALL be the sum of its individual ticket prices.

#### Scenario: Six members at 40 euros
- **WHEN** the system price is 40.00 and a group of 6 users processes `GROUPTICKET CREATE;TG02;G02;2027-04-15`
- **THEN** each of the six individual tickets costs 32.00 and the displayed total is 192.00

#### Scenario: Bracket boundaries
- **WHEN** group tickets are created for groups of 2, 3, 5, 10 and 11 members at price 40.00
- **THEN** the individual prices are 40.00, 36.00, 36.00, 32.00 and 20.00 respectively

### Requirement: GROUPTICKET DELETE command
The system SHALL accept `GROUPTICKET DELETE;<identifier>` and delete, as one consistent operation, all SpeedPasses and Special Passes of the individual tickets in its composition, those individual tickets and the group ticket itself, only when its date is strictly later than the current system date. Group tickets for the current day or earlier SHALL be retained and the deletion rejected with an error message, as SHALL an unknown identifier; a rejected deletion SHALL leave everything unchanged.

#### Scenario: Delete a future group ticket
- **WHEN** the system date is 2027-04-01, group ticket `TG01` is dated 2027-04-15 and `GROUPTICKET DELETE;TG01` is processed
- **THEN** `TG01` and all its derived individual tickets no longer exist

#### Scenario: Current-day group ticket retained
- **WHEN** the system date is 2027-04-15, `TG01` is dated 2027-04-15 and `GROUPTICKET DELETE;TG01` is processed
- **THEN** an error message is displayed and `TG01` still exists

#### Scenario: Passes of the composition deleted
- **WHEN** a member's individual ticket in future group ticket `TG01` has a SpeedPass and `GROUPTICKET DELETE;TG01` is processed
- **THEN** that SpeedPass, the individual tickets and `TG01` no longer exist

### Requirement: Composed tickets are not deleted individually
An individual ticket belonging to a group ticket's composition SHALL NOT be deletable with `TICKET DELETE`.

#### Scenario: TICKET DELETE on a derived identifier
- **WHEN** `TICKET DELETE;TG01:12345678Z` is processed for a future group ticket `TG01`
- **THEN** an error message is displayed and the individual ticket still exists

### Requirement: Group tickets block member deletion
Deleting a user SHALL be rejected while an individual ticket of an active group ticket references the user, or while the user belongs to a group locked by an active group ticket.

#### Scenario: Member of a locked group
- **WHEN** user `ana@correo.com` belongs to `G01`, `G01` has an active group ticket, and deletion of `ana@correo.com` is requested
- **THEN** the deletion is rejected and the user remains in `G01`
