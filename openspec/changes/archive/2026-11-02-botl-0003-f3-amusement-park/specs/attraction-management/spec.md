# Spec Delta

## ADDED Requirements

### Requirement: SpeedPass entry
`ATTRACTION ENTER;<attractionId>;<ticketId>;SPEED` SHALL be accepted at GENERAL and INDIVIDUAL attractions for individual tickets and at GENERAL and GROUP attractions for group tickets. Every admitted ticket (the individual ticket, or each constituent) SHALL have an unused SpeedPass; after all access conditions pass, the users enter and exactly one SpeedPass per admitted ticket is consumed and reported. If any condition fails, nobody enters and no pass is consumed.

#### Scenario: Individual SPEED entry
- **WHEN** `T001` has unused `T001-S1` and `T001-S2` and `ATTRACTION ENTER;A01;T001;SPEED` succeeds
- **THEN** exactly `T001-S1` becomes consumed and the output names it

#### Scenario: Group SPEED entry is atomic
- **WHEN** one constituent of `TG01` has no unused SpeedPass and `ATTRACTION ENTER;A01;TG01;SPEED` is executed
- **THEN** an error is shown, nobody enters, and no SpeedPass is consumed

#### Scenario: Group SPEED entry consumes one per member
- **WHEN** every constituent of a two-member `TG01` has an unused SpeedPass and group SPEED entry succeeds
- **THEN** two SpeedPasses are consumed, one per constituent, and both are reported

#### Scenario: Invalid option
- **WHEN** the fourth field is anything other than `SPEED`
- **THEN** the command is rejected

### Requirement: Special attraction entry
A SPECIAL attraction SHALL admit individual tickets only, SHALL reject the `SPEED` option, and SHALL require the ticket's unused Special Pass, which is marked consumed only after a successful entry and reported in the output.

#### Scenario: Special Pass consumed automatically
- **WHEN** `T001` has an unused Special Pass and `ATTRACTION ENTER;A02;T001` succeeds at SPECIAL attraction `A02`
- **THEN** the Special Pass becomes consumed and the output reports it

#### Scenario: Missing or used Special Pass
- **WHEN** the ticket has no Special Pass or it is consumed
- **THEN** entry is rejected

#### Scenario: SPEED rejected
- **WHEN** `ATTRACTION ENTER;A02;T001;SPEED` is executed at SPECIAL attraction `A02`
- **THEN** entry is rejected and no pass is consumed

## MODIFIED Requirements

### Requirement: Create attraction
`ATTRACTION CREATE;<attractionId>;<name>;<capacity>[;<type>]` SHALL create an attraction with a unique non-empty identifier, a non-empty name, a positive integer maximum capacity, and a type `GENERAL`, `INDIVIDUAL`, `GROUP`, or `SPECIAL` (default `GENERAL` when omitted); invalid or duplicate values SHALL be rejected.

#### Scenario: Create attraction
- **WHEN** `ATTRACTION CREATE;A01;Tornado;20` is executed and `A01` does not exist
- **THEN** attraction `A01` named `Tornado` with capacity 20 and no visitors exists

#### Scenario: Invalid capacity
- **WHEN** the capacity is `0`, negative, or not an integer
- **THEN** an error is shown and no attraction is created

#### Scenario: Typed creation
- **WHEN** `ATTRACTION CREATE;A02;Casa Magica;10;SPECIAL` is executed
- **THEN** `A02` is a SPECIAL attraction

#### Scenario: E2 syntax creates GENERAL
- **WHEN** `ATTRACTION CREATE;A01;Tornado;20` is executed
- **THEN** `A01` is a GENERAL attraction

#### Scenario: Unknown type
- **WHEN** the type is `VIP`
- **THEN** an error is shown and no attraction is created

### Requirement: Enter attraction with a ticket
`ATTRACTION ENTER;<attractionId>;<ticketId>` SHALL admit every user represented by the ticket — the holder of an individual ticket or the holders of all constituents of a group ticket — only if the attraction's type accepts the ticket kind, the ticket's date equals the system date, the free places suffice for all of them, and none of them is already present. Otherwise nobody SHALL enter.

#### Scenario: Individual entry
- **WHEN** ticket `T001` is dated on the system date and `ATTRACTION ENTER;A01;T001` is executed with free places
- **THEN** the ticket holder is present in `A01`

#### Scenario: Atomic capacity rejection
- **WHEN** `A01` has three free places and `ATTRACTION ENTER;A01;TG01` is executed for a group ticket with four constituents
- **THEN** an error is shown, nobody enters, and occupancy is unchanged

#### Scenario: Ticket not valid today
- **WHEN** the ticket's date differs from the system date
- **THEN** entry is rejected

#### Scenario: Type compatibility
- **WHEN** an individual ticket enters a GROUP attraction, or a group ticket enters an INDIVIDUAL or SPECIAL attraction
- **THEN** entry is rejected; GENERAL accepts both kinds

### Requirement: Attraction status
`ATTRACTION STATUS;<attractionId>` SHALL display the identifier, name, type, maximum capacity, current number of users present, available places, and the users present, reflecting the current state regardless of whether users entered individually or with a group ticket.

#### Scenario: Status after entry and exit
- **WHEN** `T001` (1 user) and `TG01` (2 users) entered capacity-20 attraction `A01`, status is queried, then `ATTRACTION EXIT;A01;T001` is executed and status is queried again
- **THEN** the first status shows 3 present and 17 available listing all three users, and the second shows 2 present and 18 available

### Requirement: Attraction persistence
Attraction definitions (identifier, name, capacity, type) SHALL be persisted and restored at startup; the users present SHALL NOT be persisted, so every attraction starts empty.

#### Scenario: Restart empties attractions
- **WHEN** users are inside `A01` and the application restarts
- **THEN** `A01` exists with the same name, capacity, and type and no users present

#### Scenario: E2 file without type
- **WHEN** `attractions.txt` contains a line without the type column
- **THEN** that attraction loads as GENERAL
