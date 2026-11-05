## ADDED Requirements

### Requirement: Individual entry with a SpeedPass
`ATTRACTION ENTER;<attraction>;<ticket>;SPEED` with an individual ticket at a GENERAL or INDIVIDUAL attraction SHALL require, besides the previous conditions, at least one unconsumed SpeedPass associated with the ticket; on success exactly one such SpeedPass SHALL be marked consumed and the output SHALL name it. If the entry fails for any reason no SpeedPass is modified. Any fourth element other than `SPEED` is rejected with an error message starting with `ERROR`.

#### Scenario: SpeedPass consumed on entry
- **GIVEN** ticket `T001` valid today with unconsumed SpeedPasses `SP01:1` and `SP02:1`
- **WHEN** `ATTRACTION ENTER;A01;T001;SPEED` is processed
- **THEN** `ENTRY GRANTED` is printed naming the consumed SpeedPass, exactly one of the two is consumed and Ana is inside

#### Scenario: No available SpeedPass
- **GIVEN** ticket `T001` whose SpeedPasses are all consumed
- **WHEN** `ATTRACTION ENTER;A01;T001;SPEED` is processed
- **THEN** an error message starting with `ERROR` is printed and nobody enters

#### Scenario: Failure leaves passes untouched
- **GIVEN** ticket `T001` with an unconsumed SpeedPass and a full attraction `A02`
- **WHEN** `ATTRACTION ENTER;A02;T001;SPEED` is processed
- **THEN** an error message starting with `ERROR` is printed and the SpeedPass is still unconsumed

### Requirement: Atomic group entry with SpeedPasses
`ATTRACTION ENTER;<attraction>;<groupTicket>;SPEED` at a GENERAL or GROUP attraction SHALL, before changing any state, check that the group ticket is valid today, the attraction accepts group tickets, there is capacity for all members and each composition ticket has an unconsumed SpeedPass. If all hold, all members enter and exactly one SpeedPass of each composition ticket is consumed; otherwise no member enters and no SpeedPass is consumed.

#### Scenario: All members have SpeedPasses
- **GIVEN** group ticket `TG01` (Ana, Pedro) valid today and each composition ticket with one unconsumed SpeedPass
- **WHEN** `ATTRACTION ENTER;A03;TG01;SPEED` is processed
- **THEN** both enter and both SpeedPasses are consumed

#### Scenario: One member lacks a SpeedPass
- **GIVEN** only Ana's composition ticket has an unconsumed SpeedPass
- **WHEN** `ATTRACTION ENTER;A03;TG01;SPEED` is processed
- **THEN** an error message starting with `ERROR` is printed, nobody enters and Ana's SpeedPass is still unconsumed

### Requirement: Special attraction entry with the Special Pass
Entry to a SPECIAL attraction with `ATTRACTION ENTER;<attraction>;<individualTicket>` SHALL automatically find the ticket's Special Pass and require it to be unconsumed; it SHALL be marked consumed only when the entry succeeds, and the output SHALL state that the Special Pass was consumed. A ticket without Special Pass, with a consumed one, or failing any other condition is rejected without changing the pass. The `SPEED` variant and group tickets are not permitted at SPECIAL attractions.

#### Scenario: Special Pass consumed on entry
- **GIVEN** ticket `T001` valid today with unconsumed Special Pass `PE01`
- **WHEN** `ATTRACTION ENTER;A04;T001` is processed
- **THEN** `ENTRY GRANTED` is printed stating that Special Pass `PE01` was consumed, and `PE01` is consumed

#### Scenario: Missing or consumed Special Pass
- **WHEN** a ticket without Special Pass, or whose Special Pass is consumed, enters `A04`
- **THEN** an error message starting with `ERROR` is printed and nobody enters

#### Scenario: SPEED not allowed at special attractions
- **WHEN** `ATTRACTION ENTER;A04;T001;SPEED` is processed
- **THEN** an error message starting with `ERROR` is printed and neither the Special Pass nor any SpeedPass changes
