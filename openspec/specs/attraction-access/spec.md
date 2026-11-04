# attraction-access Specification

## Purpose
Defines how visitors enter and leave attractions using individual or group tickets valid for the configured system date, without ever exceeding capacity.

## Requirements

### Requirement: Entry with a ticket
`ATTRACTION ENTER;IdAtraccion;IdTicket` SHALL admit the users represented by the ticket: the owner for an individual ticket, or the owners of all constituent tickets for a group ticket. Individual and group tickets SHALL be handled by the same entry operation. Unknown attractions or tickets SHALL be rejected.

#### Scenario: Individual entry
- **GIVEN** system date 2026-10-15, attraction `A01` with free places and ticket `T001` for Ana dated 2026-10-15
- **WHEN** `ATTRACTION ENTER;A01;T001` is processed
- **THEN** an `OK` message is produced and Ana is present in `A01`

#### Scenario: Group entry
- **GIVEN** group ticket `TG01` dated 2026-10-15 composed of the tickets of Pedro and Luis
- **WHEN** `ATTRACTION ENTER;A01;TG01` is processed
- **THEN** Pedro and Luis are present in `A01`

#### Scenario: Unknown ticket or attraction
- **WHEN** `ATTRACTION ENTER;A01;T999` or `ATTRACTION ENTER;A99;T001` is processed
- **THEN** an `ERROR` message is produced and nobody enters

### Requirement: Ticket valid for the system date
A ticket SHALL be accepted for entry only when its date equals the current system date.

#### Scenario: Ticket for another day
- **GIVEN** ticket `T001` dated 2026-10-15
- **WHEN** the system date is 2026-10-14 or 2026-10-16 and `ATTRACTION ENTER;A01;T001` is processed
- **THEN** an `ERROR` message is produced and nobody enters

### Requirement: Capacity and atomic group entry
An entry SHALL be rejected as a whole if admitting all the ticket's users would exceed the attraction's maximum capacity, or if any of them is already present in that attraction; otherwise all of them SHALL be admitted.

#### Scenario: Insufficient capacity for a group
- **GIVEN** `A01` has three places left and `TG01` represents four users
- **WHEN** `ATTRACTION ENTER;A01;TG01` is processed
- **THEN** an `ERROR` message is produced and none of the four users enters

#### Scenario: Exactly full
- **GIVEN** `A01` has two places left and `TG01` represents two users
- **WHEN** `ATTRACTION ENTER;A01;TG01` is processed
- **THEN** both enter and `A01` has zero available places

#### Scenario: Already present
- **GIVEN** Ana entered `A01` with `T001`
- **WHEN** `ATTRACTION ENTER;A01;T001` is processed again
- **THEN** an `ERROR` message is produced and Ana is counted once

### Requirement: Exit with a ticket
`ATTRACTION EXIT;IdAtraccion;IdTicket` SHALL remove from the attraction the ticket's users who are present and free their places, without checking the ticket date. It SHALL be rejected if the attraction or ticket is unknown or none of the ticket's users is present.

#### Scenario: Individual exit
- **GIVEN** Ana is present in `A01` with `T001`
- **WHEN** the system date changes to 2026-10-16 and `ATTRACTION EXIT;A01;T001` is processed
- **THEN** an `OK` message is produced and Ana is no longer present

#### Scenario: Group exit
- **GIVEN** Pedro and Luis entered `A01` with `TG01`
- **WHEN** `ATTRACTION EXIT;A01;TG01` is processed
- **THEN** both leave and two places are freed

#### Scenario: Nobody present
- **WHEN** `ATTRACTION EXIT;A01;T001` is processed and Ana is not present
- **THEN** an `ERROR` message is produced
