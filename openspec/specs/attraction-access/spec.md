# attraction-access Specification

## Purpose

Lets visitors enter attractions with individual or group tickets valid for the system date, respecting capacity, and leave them again.

## Requirements

### Requirement: Entry with a ticket
The command `ATTRACTION ENTER;IdAtraccion;IdTicket` SHALL admit the users represented by the ticket: the owner of an individual ticket, or the users of the individual tickets composing a group ticket. Both ticket kinds SHALL be handled by the same access logic.

#### Scenario: Individual entry
- **GIVEN** system date 2026-10-15, attraction `A01` with free places and Ana's ticket `T001` dated 2026-10-15
- **WHEN** `ATTRACTION ENTER;A01;T001` is processed
- **THEN** Ana is present in `A01`

#### Scenario: Group entry
- **GIVEN** group ticket `TG01` dated 2026-10-15 composed of the tickets of Pedro and Luis
- **WHEN** `ATTRACTION ENTER;A01;TG01` is processed
- **THEN** Pedro and Luis are present in `A01`

### Requirement: Ticket valid for the system date
Entry SHALL be allowed only with a ticket whose date equals the current system date; an unknown ticket or attraction SHALL be rejected.

#### Scenario: Ticket for another day
- **GIVEN** system date 2026-10-14 and `T001` dated 2026-10-15
- **WHEN** `ATTRACTION ENTER;A01;T001` is processed
- **THEN** an error is reported and nobody enters

#### Scenario: Unknown ticket or attraction
- **WHEN** `ATTRACTION ENTER;A99;T001` or `ATTRACTION ENTER;A01;T999` is processed
- **THEN** an error is reported

### Requirement: Capacity and atomic entry
An entry SHALL be rejected as a whole if admitting all the ticket's users would exceed the maximum capacity, or if any of them is already present in that attraction; in that case no user SHALL enter.

#### Scenario: Group does not fit
- **GIVEN** `A01` with 3 free places and `TG01` representing 4 users
- **WHEN** `ATTRACTION ENTER;A01;TG01` is processed
- **THEN** the operation is rejected and the number of users present is unchanged

#### Scenario: Exactly full
- **GIVEN** `A01` with 2 free places and a group ticket of 2 users valid today
- **WHEN** it is used to enter
- **THEN** both users enter and no places remain

#### Scenario: Already present
- **GIVEN** Ana is present in `A01`
- **WHEN** `ATTRACTION ENTER;A01;T001` is processed again
- **THEN** an error is reported and Ana is counted once

### Requirement: Exit
The command `ATTRACTION EXIT;IdAtraccion;IdTicket` SHALL remove from the attraction the users of that ticket who are present and free their places, without checking the ticket date. The email of a present user SHALL also be accepted in place of the ticket identifier. If none of the designated users is present, the command SHALL be rejected.

#### Scenario: Exit releases capacity
- **GIVEN** Ana entered `A01` with `T001`
- **WHEN** `ATTRACTION EXIT;A01;T001` is processed
- **THEN** Ana is no longer present and one more place is available

#### Scenario: Group exit
- **GIVEN** Pedro and Luis entered with `TG01`
- **WHEN** `ATTRACTION EXIT;A01;TG01` is processed
- **THEN** both leave

#### Scenario: Exit by email
- **GIVEN** Ana is present in `A01`
- **WHEN** `ATTRACTION EXIT;A01;ana@correo.com` is processed
- **THEN** Ana is no longer present

#### Scenario: Nobody to exit
- **WHEN** `ATTRACTION EXIT;A01;T001` is processed and Ana is not present
- **THEN** an error is reported
