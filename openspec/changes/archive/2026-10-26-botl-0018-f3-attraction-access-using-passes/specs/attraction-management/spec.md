# Spec Delta

## MODIFIED Requirements

### Requirement: Enter attraction
The command `ATTRACTION ENTER;<attractionId>;<ticketId>` SHALL add to the attraction every user covered by the ticket — the user of an individual ticket, or the users of the individual tickets composing a group ticket — using the same entry logic for both ticket kinds, without using SpeedPasses, and print `OK ENTERED <attractionId> -> <n> user(s)`. The form `ATTRACTION ENTER;<attractionId>;<ticketId>;SPEED` SHALL request SpeedPass use; any other fourth value SHALL be rejected. The command SHALL be rejected with an `ERROR` line if the attraction or ticket does not exist.

#### Scenario: Individual entry
- **WHEN** ticket `T001` of Ana is dated on the system date and `ATTRACTION ENTER;A01;T001` is processed
- **THEN** Ana is present in `A01` and none of her SpeedPasses is consumed

#### Scenario: Group entry
- **WHEN** group ticket `TG01` covering Pedro and Luis is dated on the system date and `ATTRACTION ENTER;A01;TG01` is processed
- **THEN** Pedro and Luis are present in `A01`

#### Scenario: Invalid mode
- **WHEN** `ATTRACTION ENTER;A01;T001;FAST` is processed
- **THEN** an `ERROR` line is printed and nobody enters

## ADDED Requirements

### Requirement: Individual entry with SpeedPass
At GENERAL and INDIVIDUAL attractions, `ATTRACTION ENTER;<attractionId>;<ticketId>;SPEED` with an individual ticket SHALL consume exactly one unused SpeedPass of that ticket when the entry succeeds. If the ticket has no unused SpeedPass, the entry SHALL be rejected and nobody enters.

#### Scenario: One SpeedPass consumed
- **WHEN** `T001` has unused SpeedPasses `SP1` and `SP2` and `ATTRACTION ENTER;A01;T001;SPEED` succeeds
- **THEN** exactly one of them is marked consumed and the output reports it

#### Scenario: No SpeedPass left
- **WHEN** all SpeedPasses of `T001` are consumed and `ATTRACTION ENTER;A01;T001;SPEED` is processed
- **THEN** an `ERROR` line is printed and nobody enters

### Requirement: Group entry with SpeedPasses is atomic
At GENERAL and GROUP attractions, `ATTRACTION ENTER;<attractionId>;<groupTicketId>;SPEED` SHALL require every individual ticket of the group ticket's composition to have at least one unused SpeedPass and SHALL consume one per member when the entry succeeds. If any member lacks an unused SpeedPass or any other access condition fails (date, capacity, presence), no user SHALL enter and no SpeedPass SHALL be consumed.

#### Scenario: Every member has a SpeedPass
- **WHEN** both internal tickets of `TG01` have an unused SpeedPass and the group enters with `SPEED`
- **THEN** both users enter and one SpeedPass per member is consumed

#### Scenario: One member without SpeedPass
- **WHEN** only one of the two internal tickets of `TG01` has an unused SpeedPass
- **THEN** the entry with `SPEED` is rejected, nobody enters and no SpeedPass is consumed

#### Scenario: Capacity failure consumes nothing
- **WHEN** every member has a SpeedPass but the attraction lacks places for the whole group
- **THEN** the entry is rejected and no SpeedPass is consumed

### Requirement: Special attraction access with the Special Pass
SPECIAL attractions SHALL admit individual entries only: the system SHALL locate the ticket's Special Pass automatically, reject the entry if the ticket has none or it is already consumed, and mark it consumed only after the entry succeeds. The `SPEED` option SHALL be rejected at SPECIAL attractions.

#### Scenario: Special Pass consumed on entry
- **WHEN** `T001` has an unused Special Pass `XP1` and `ATTRACTION ENTER;A04;T001` succeeds at SPECIAL attraction `A04`
- **THEN** `XP1` is marked consumed and the output reports it

#### Scenario: Missing or used Special Pass
- **WHEN** `T001` has no Special Pass, or it is already consumed
- **THEN** the entry at `A04` is rejected and nobody enters

#### Scenario: SPEED at a special attraction
- **WHEN** `ATTRACTION ENTER;A04;T001;SPEED` is processed
- **THEN** an `ERROR` line is printed, nobody enters and no pass is consumed

#### Scenario: Failed special entry keeps the pass
- **WHEN** `A04` is full and `T001` with an unused Special Pass tries to enter
- **THEN** the entry is rejected and the Special Pass stays unconsumed

### Requirement: Consumption reported and stored
A successful entry that consumes passes SHALL report them in its output (`OK ENTERED <attractionId> -> <n> user(s), consumed <kind> <ids>`), and the new consumption status SHALL be persisted. A failed entry SHALL NOT consume any pass.

#### Scenario: Output and persistence
- **WHEN** an entry with `SPEED` consumes `SP1` and the application restarts
- **THEN** the output mentions `SP1` and after the restart `SP1` is still consumed
