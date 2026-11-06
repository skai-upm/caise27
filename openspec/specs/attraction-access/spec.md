# attraction-access Specification

## Purpose

Defines how users get into Amusement Park attractions by presenting a ticket: which tickets are valid, which users are admitted and how the attraction's capacity limits entry.

## Requirements

### Requirement: Enter an attraction with an individual ticket
`ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier` with an existing individual ticket SHALL admit the ticket's user into the existing attraction when the ticket is valid for the current system date and the attraction has free capacity, and print a confirmation naming the user's email.

#### Scenario: Valid ticket and free capacity
- **GIVEN** the system date is `2027-04-15`, attraction `A01` has capacity 20 and 0 users inside, and Ana's ticket `T001` is dated `2027-04-15`
- **WHEN** `ATTRACTION ENTER;A01;T001` is processed
- **THEN** a confirmation naming `ana@correo.com` is printed and `ATTRACTION LIST;A01` shows `ana@correo.com`

#### Scenario: Unknown attraction or ticket
- **WHEN** `ATTRACTION ENTER;A99;T001` or `ATTRACTION ENTER;A01;T999` is processed
- **THEN** an error naming the missing attraction or ticket is printed and nobody enters

### Requirement: Ticket valid only on its date
A ticket SHALL be valid for entry only when its date equals the current system date; the date MUST be checked only at the moment of entry, and a ticket for another date MUST be rejected with an error and nobody enters.

#### Scenario: Ticket for a different date
- **GIVEN** the system date is `2027-04-15` and ticket `T002` is dated `2027-04-16` (or `2027-04-14`)
- **WHEN** `ATTRACTION ENTER;A01;T002` is processed
- **THEN** an error reporting that the ticket is not valid on `2027-04-15` is printed and nobody enters

#### Scenario: Presence independent of later date changes
- **GIVEN** Ana entered `A01` with a ticket valid on `2027-04-15`
- **WHEN** `SYSTEM DATE;2027-04-20` is processed
- **THEN** `ATTRACTION LIST;A01` still shows `ana@correo.com`

### Requirement: Capacity limits entry
The attraction MUST have free capacity for every user who needs to enter; otherwise entry SHALL be rejected with an error and nobody enters. A user already inside the attraction MUST NOT enter it again.

#### Scenario: Full attraction
- **GIVEN** attraction `A03` with capacity 1 and one user inside
- **WHEN** `ATTRACTION ENTER;A03;T001` is processed with Ana's valid ticket
- **THEN** an error reporting insufficient capacity is printed and Ana is not inside

#### Scenario: Already inside
- **GIVEN** Ana is inside `A01`
- **WHEN** she presents another valid ticket for `A01`
- **THEN** an error reporting that she is already inside is printed and she appears once

### Requirement: Access through the common ticket abstraction
Access SHALL use the behavior common to all ticket types — the users a ticket admits and its validity date — rather than a mechanism specific to one ticket type.

#### Scenario: Individual ticket admits its user
- **WHEN** the users admitted by an individual ticket are requested through the common ticket abstraction
- **THEN** exactly the ticket's user is returned

### Requirement: Enter an attraction with a group ticket
`ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier` with an existing group ticket valid for the current system date SHALL admit together every user of the individual tickets in its composition, using the same access operation as individual tickets. Access MUST be indivisible: if the attraction lacks capacity for all of them, or any of them is already inside, nobody enters and an error is printed.

#### Scenario: Whole group enters
- **GIVEN** group ticket `TG01` valid today with Ana and Pedro, and attraction `A01` with capacity 20 and nobody inside
- **WHEN** `ATTRACTION ENTER;A01;TG01` is processed
- **THEN** a confirmation naming `ana@correo.com` and `pedro@correo.com` is printed and both are inside `A01`

#### Scenario: Insufficient capacity for the whole group
- **GIVEN** group ticket `TG01` valid today with Ana and Pedro, and attraction `A03` with capacity 2 and one other user inside
- **WHEN** `ATTRACTION ENTER;A03;TG01` is processed
- **THEN** an error reporting insufficient capacity is printed and neither Ana nor Pedro is inside

#### Scenario: Group ticket for another date
- **GIVEN** group ticket `TG02` dated tomorrow
- **WHEN** `ATTRACTION ENTER;A01;TG02` is processed
- **THEN** an error reporting that the ticket is not valid today is printed and nobody enters

### Requirement: Entry option SPEED
`ATTRACTION ENTER;<attraction>;<ticket>;SPEED` SHALL request entry using SpeedPasses; the form without option keeps its behavior. Any option other than `SPEED` MUST be rejected with an error. `SPEED` MUST be rejected at `SPECIAL` attractions.

#### Scenario: Unknown option
- **WHEN** `ATTRACTION ENTER;A01;T001;FAST` is processed
- **THEN** an error reporting an invalid entry option is printed and nobody enters

#### Scenario: SPEED at a special attraction
- **GIVEN** ticket `T001` valid today with an unconsumed Special Pass and SpeedPass
- **WHEN** `ATTRACTION ENTER;A04;T001;SPEED` is processed at special attraction `A04`
- **THEN** an error reporting that `A04` does not accept SpeedPasses is printed and no pass changes

### Requirement: Individual entry with SpeedPass
Individual SPEED entry at an attraction that accepts SpeedPasses SHALL require at least one unconsumed SpeedPass on the ticket; when entry succeeds exactly one available SpeedPass MUST be marked consumed and named in the output. If entry fails for any reason, no SpeedPass is modified.

#### Scenario: One SpeedPass consumed
- **GIVEN** ticket `T001` valid today with unconsumed SpeedPasses `SP01:1` and `SP01:2`
- **WHEN** `ATTRACTION ENTER;A01;T001;SPEED` is processed
- **THEN** Ana enters, exactly one of the two passes is consumed and the output names it

#### Scenario: No SpeedPass available
- **GIVEN** ticket `T001` valid today whose SpeedPasses are all consumed (or none)
- **WHEN** `ATTRACTION ENTER;A01;T001;SPEED` is processed
- **THEN** an error reporting the missing SpeedPass is printed and nobody enters

#### Scenario: Failed entry keeps passes
- **GIVEN** ticket `T001` with an unconsumed SpeedPass and a full attraction `A05`
- **WHEN** `ATTRACTION ENTER;A05;T001;SPEED` is processed
- **THEN** an error reporting insufficient capacity is printed and the SpeedPass is still unconsumed

### Requirement: Group entry with SpeedPasses
Group SPEED entry at `GENERAL` and `GROUP` attractions SHALL require, before any change, a valid group ticket, an attraction accepting group tickets, capacity for all members and an unconsumed SpeedPass on every constituent ticket; then all members enter and exactly one SpeedPass of each constituent ticket is consumed, as one indivisible operation. If any condition fails, no member enters and no SpeedPass is consumed.

#### Scenario: Whole group with SpeedPasses
- **GIVEN** group ticket `TG01` (Ana, Pedro) valid today with one unconsumed SpeedPass on each constituent
- **WHEN** `ATTRACTION ENTER;A03;TG01;SPEED` is processed
- **THEN** both enter and both SpeedPasses are consumed

#### Scenario: One member without SpeedPass
- **GIVEN** group ticket `TG01` where only Ana's constituent has an unconsumed SpeedPass
- **WHEN** `ATTRACTION ENTER;A01;TG01;SPEED` is processed
- **THEN** an error naming Pedro's constituent ticket is printed, nobody enters, and Ana's SpeedPass is still unconsumed

### Requirement: Special Pass consumed at special attractions
Entry to a `SPECIAL` attraction with the normal syntax SHALL automatically use the ticket's unconsumed Special Pass, mark it consumed only when entry succeeds, and report its consumption. Without a Special Pass, with a consumed one, or if any other condition fails, entry MUST be rejected without changing the pass.

#### Scenario: Special Pass consumed
- **GIVEN** ticket `T001` valid today with unconsumed Special Pass `PE01`
- **WHEN** `ATTRACTION ENTER;A04;T001` is processed
- **THEN** Ana enters, `PE01` is consumed and the output says so

#### Scenario: Already consumed
- **GIVEN** `PE01` was consumed by a previous entry
- **WHEN** `ATTRACTION ENTER;A04;T001` is processed again after Ana exits
- **THEN** an error is printed and nobody enters

### Requirement: Constituent tickets usable for individual entry
An individual ticket that belongs to a group ticket's composition SHALL be usable on its own with `ATTRACTION ENTER` as an individual ticket (admitting only its user, with its own passes), so that personal passes bought for it can be used; the group ticket itself remains subject to each attraction type's restrictions.

#### Scenario: Special Pass on a constituent ticket
- **GIVEN** constituent ticket `TG01:12345678Z` valid today with unconsumed Special Pass `PE01`
- **WHEN** `ATTRACTION ENTER;A04;TG01:12345678Z` is processed at special attraction `A04`
- **THEN** Ana enters and `PE01` is consumed, while `ATTRACTION ENTER;A04;TG01` is still rejected
