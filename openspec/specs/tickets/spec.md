# tickets Specification

## Purpose
Defines park tickets: the information and behavior common to every ticket type, the shared identifier space, individual ticket purchase and deletion, and the effect of active tickets on user deletion.

## Requirements

### Requirement: Common ticket information and identifier space
Every ticket SHALL have a unique identifier, a date of use and a price. All ticket types SHALL share one identifier space: creating any ticket whose identifier is already used by a ticket of any type SHALL be rejected, leaving existing tickets unchanged.

#### Scenario: Identifier reused
- **GIVEN** ticket `T001` exists
- **WHEN** `TICKET CREATE;T001;ana@correo.com;2027-04-20` is processed
- **THEN** an error is shown and the existing `T001` is unchanged

### Requirement: Individual ticket creation
The command `TICKET CREATE;<identifier>;<email>;<date>` SHALL create a ticket for the existing user with that e-mail, valid only on `<date>` (format `yyyy-MM-dd`), priced at the current base price, and SHALL display that price. It SHALL be rejected with an error and no ticket created when the identifier is empty or already used, the user does not exist, or the date is invalid or earlier than the current system date.

#### Scenario: Ticket created at the initial price
- **GIVEN** the system date is 2027-04-15, `SYSTEM PRICE` has not been used and `ana@correo.com` is registered
- **WHEN** `TICKET CREATE;T001;ana@correo.com;2027-04-15` is processed
- **THEN** ticket `T001` for that user and date exists with price 40.00 and the output shows 40.00

#### Scenario: Ticket uses the configured price
- **GIVEN** `SYSTEM PRICE;45.50` has been processed
- **WHEN** a valid `TICKET CREATE` is processed
- **THEN** the ticket price is 45.50

#### Scenario: Past date rejected
- **GIVEN** the system date is 2027-04-15
- **WHEN** `TICKET CREATE;T002;ana@correo.com;2027-04-14` is processed
- **THEN** an error is shown and no ticket `T002` exists

#### Scenario: Unknown user rejected
- **WHEN** `TICKET CREATE;T003;nobody@correo.com;2027-04-20` is processed
- **THEN** an error is shown and no ticket `T003` exists

#### Scenario: Later price change does not affect existing tickets
- **GIVEN** ticket `T001` was created at 40.00
- **WHEN** `SYSTEM PRICE;50` is processed
- **THEN** `T001` still costs 40.00

### Requirement: Individual ticket deletion
The command `TICKET DELETE;<identifier>` SHALL delete an individual ticket only when its date is strictly later than the current system date. Tickets dated today or earlier SHALL be kept as historical records and the deletion rejected; unknown identifiers SHALL be rejected. Individual tickets that are part of a group ticket SHALL NOT be deleted with `TICKET DELETE`; they are deleted only with their group ticket. When a ticket is deleted, every SpeedPass and the Special Pass associated with it SHALL be deleted with it, even if they were bought in different operations.

#### Scenario: Future ticket deleted
- **GIVEN** the system date is 2027-04-15 and ticket `T001` is dated 2027-04-20
- **WHEN** `TICKET DELETE;T001` is processed
- **THEN** a confirmation is shown and `T001` no longer exists

#### Scenario: Today's or past ticket kept
- **GIVEN** ticket `T001` dated 2027-04-15 and the system date 2027-04-15 (or later)
- **WHEN** `TICKET DELETE;T001` is processed
- **THEN** an error is shown and `T001` still exists

#### Scenario: Unknown ticket
- **WHEN** `TICKET DELETE;T999` is processed and no such ticket exists
- **THEN** an error is shown

#### Scenario: Component of a group ticket
- **GIVEN** future group ticket `TG01` with component `TG01:12345678Z`
- **WHEN** `TICKET DELETE;TG01:12345678Z` is processed
- **THEN** an error is shown and the component still exists

#### Scenario: Passes deleted with their ticket
- **GIVEN** future ticket `T001` with SpeedPasses `SP01:1` and `SP02:1`, and Special Pass `PE01`
- **WHEN** `TICKET DELETE;T001` is processed
- **THEN** `T001`, `SP01:1`, `SP02:1` and `PE01` no longer exist

### Requirement: Active tickets block user deletion
A ticket SHALL be active when its date is equal to or later than the current system date. Deleting a user SHALL be rejected while any active ticket is associated with that user, including individual tickets that make up group tickets, and while the user belongs to a group locked by an active group ticket. Historical tickets alone SHALL NOT block it. This locked state SHALL be derived from the tickets managed by the system and never stored on the user. When deletion is allowed, the user SHALL be removed from every group they belong to, and historical tickets SHALL be retained.

#### Scenario: Active ticket blocks deletion
- **GIVEN** `ana@correo.com` has a ticket dated on the current system date
- **WHEN** deletion of that user is requested
- **THEN** the deletion is rejected

#### Scenario: Only historical tickets
- **GIVEN** all tickets of `ana@correo.com` are dated before the current system date
- **WHEN** deletion of that user is requested
- **THEN** the ticket rule does not block the deletion and the tickets are retained

#### Scenario: Member of a locked group
- **GIVEN** `ana@correo.com` belongs to group `G01`, which has an active group ticket
- **WHEN** deletion of that user is requested
- **THEN** the deletion is rejected and `G01` still contains the user

#### Scenario: Removed from modifiable groups
- **GIVEN** `ana@correo.com` belongs only to unlocked groups and has no active tickets
- **WHEN** deletion of that user is performed
- **THEN** the user no longer belongs to any group

### Requirement: Group ticket purchase
The command `GROUPTICKET CREATE;<identifier>;<group>;<date>` SHALL create a group ticket for an existing, non-empty group on a date not earlier than the current system date. It SHALL be composed of one new individual ticket per current member, identified `<identifier>:<dni>`, holding a reference to that member. It SHALL keep a reference to the purchasing group, and its composition SHALL NOT change afterwards. The command SHALL display the total price. Missing groups, empty groups, past or invalid dates and used identifiers SHALL be rejected with an error.

#### Scenario: Composition with derived identifiers
- **GIVEN** group `G01` with members whose DNIs are `12345678Z` and `87654321X`, and the system date 2027-04-15
- **WHEN** `GROUPTICKET CREATE;TG01;G01;2027-04-15` is processed
- **THEN** group ticket `TG01` exists, composed of tickets `TG01:12345678Z` and `TG01:87654321X` for those members and that date

#### Scenario: Empty group rejected
- **GIVEN** group `G02` with no members
- **WHEN** `GROUPTICKET CREATE;TG02;G02;2027-04-20` is processed
- **THEN** an error is shown and no ticket `TG02` exists

#### Scenario: Past date or unknown group rejected
- **WHEN** a `GROUPTICKET CREATE` names a date before the current system date or a non-existent group
- **THEN** an error is shown and no ticket is created

#### Scenario: Composition is fixed at purchase
- **GIVEN** group ticket `TG01` dated in the past, composed for members A and B
- **WHEN** a member C is later added to the group
- **THEN** `TG01` is still composed of tickets for A and B only

### Requirement: Atomic identifier check for group tickets
Before a group ticket is created, its identifier and every derived component identifier SHALL be checked against the shared identifier space. If any of them is already used, the group ticket SHALL NOT be created, not even partially.

#### Scenario: Derived identifier conflict
- **GIVEN** an existing ticket `TG01:87654321X` and group `G01` with members whose DNIs are `12345678Z` and `87654321X`
- **WHEN** `GROUPTICKET CREATE;TG01;G01;2027-04-20` is processed
- **THEN** an error is shown, and neither `TG01` nor `TG01:12345678Z` exists

#### Scenario: Group ticket identifier already used
- **GIVEN** individual ticket `TG01` exists
- **WHEN** `GROUPTICKET CREATE;TG01;G01;2027-04-20` is processed
- **THEN** an error is shown and the existing ticket is unchanged

### Requirement: Group discount
Each component ticket SHALL cost the current base price minus a discount that depends on the number of members in the purchase: fewer than 3 → 0%, 3 to 5 → 10%, 6 to 10 → 20%, more than 10 → 50%. The group ticket total SHALL be the sum of its component prices. Later price changes SHALL NOT affect it.

#### Scenario: Six members at 40 euros
- **GIVEN** the base price is 40.00 and group `G01` has 6 members
- **WHEN** its group ticket is purchased
- **THEN** each component costs 32.00, and the total charged and displayed is 192.00

#### Scenario: Bracket limits
- **WHEN** group tickets are bought for 2, 3, 5, 6, 10 and 11 members at 40.00
- **THEN** the per-member prices are 40.00, 36.00, 36.00, 32.00, 32.00 and 20.00

### Requirement: Group ticket deletion
The command `GROUPTICKET DELETE;<identifier>` SHALL delete a group ticket only when its date is strictly later than the current system date, together with all its component tickets and every SpeedPass and Special Pass associated with those components. All checks SHALL happen before anything is removed, so the deletion is never partial. Today's and past group tickets SHALL be kept as historical records, and unknown identifiers SHALL be rejected.

#### Scenario: Future group ticket deleted with its composition
- **GIVEN** group ticket `TG01` dated after the current system date
- **WHEN** `GROUPTICKET DELETE;TG01` is processed
- **THEN** `TG01` and all its `TG01:<dni>` tickets no longer exist, and the group can be modified again if it has no other active group ticket

#### Scenario: Today's group ticket kept
- **GIVEN** group ticket `TG01` dated on the current system date
- **WHEN** `GROUPTICKET DELETE;TG01` is processed
- **THEN** an error is shown and `TG01` still exists

#### Scenario: Component passes deleted with the group ticket
- **GIVEN** future group ticket `TG01`, whose component `TG01:12345678Z` has SpeedPass `SP05:1`
- **WHEN** `GROUPTICKET DELETE;TG01` is processed
- **THEN** `SP05:1`, the components and `TG01` no longer exist

### Requirement: Historical group tickets survive group deletion
When a group is deleted, its group tickets SHALL be kept with their identifier, date, price and composition, and their reference to the group SHALL become empty.

#### Scenario: Group deleted after its ticket became historical
- **GIVEN** group `G01` whose only group ticket `TG01` is dated before the current system date
- **WHEN** `GROUP DELETE;G01` is processed
- **THEN** `G01` no longer exists, and `TG01` keeps its identifier, date, price and components with no group reference
