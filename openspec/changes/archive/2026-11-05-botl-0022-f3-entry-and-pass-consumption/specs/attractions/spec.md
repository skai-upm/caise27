# Spec Delta

## ADDED Requirements

### Requirement: Entry with a SpeedPass
The command `ATTRACTION ENTER;<attraction>;<ticket>;SPEED` SHALL keep every check of normal entry: the attraction and ticket exist, the ticket is valid today, the type accepts the ticket kind, and there is capacity. It SHALL also require an unconsumed SpeedPass for the individual ticket. On success, exactly one of its unconsumed SpeedPasses SHALL be marked as consumed, and the output SHALL name it. If entry fails for any reason, no SpeedPass SHALL change. A fourth field other than `SPEED` SHALL be rejected as invalid syntax. Entry without `SPEED` SHALL never consume SpeedPasses.

#### Scenario: SpeedPass consumed on entry
- **GIVEN** GENERAL attraction `A01` and ticket `T001`, valid today, with unconsumed SpeedPasses `SP01:1` and `SP02:1`
- **WHEN** `ATTRACTION ENTER;A01;T001;SPEED` is processed
- **THEN** the user enters, exactly one of the two passes is consumed, and the output names the consumed pass

#### Scenario: No available SpeedPass
- **GIVEN** ticket `T002` whose SpeedPasses are all consumed, or which has none
- **WHEN** `ATTRACTION ENTER;A01;T002;SPEED` is processed
- **THEN** an error is shown and no one enters

#### Scenario: Failed entry consumes nothing
- **GIVEN** a full attraction, and ticket `T001` with an unconsumed SpeedPass
- **WHEN** `ATTRACTION ENTER` with `SPEED` is processed for it
- **THEN** an error is shown and the SpeedPass is still unconsumed

### Requirement: Group entry with SpeedPasses
`ATTRACTION ENTER;<attraction>;<groupTicket>;SPEED` SHALL be allowed at GENERAL and GROUP attractions. Before changing any state, it SHALL check that the group ticket is valid today, the type accepts group tickets, there is capacity for all members, and every individual ticket in the composition has an unconsumed SpeedPass. Entry and consumption SHALL be indivisible: on success all members enter and exactly one SpeedPass of each component is consumed; on any failure no member enters and no SpeedPass is consumed.

#### Scenario: Whole group uses SpeedPasses
- **GIVEN** GROUP attraction `A03`, and group ticket `TG01` valid today whose two components each have an unconsumed SpeedPass
- **WHEN** `ATTRACTION ENTER;A03;TG01;SPEED` is processed
- **THEN** both members enter and one SpeedPass per component is consumed

#### Scenario: One member lacks a SpeedPass
- **GIVEN** group ticket `TG01` where one component has no unconsumed SpeedPass
- **WHEN** `ATTRACTION ENTER;A03;TG01;SPEED` is processed
- **THEN** an error is shown, no member enters and no SpeedPass is consumed

## MODIFIED Requirements

### Requirement: Special attractions require a Special Pass
A SPECIAL attraction SHALL admit an individual ticket only if that ticket has an unconsumed Special Pass. The pass is used automatically, with no extra option in the entry command. The Special Pass SHALL be marked as consumed only when entry completes successfully, and the output SHALL say which Special Pass was consumed. Group tickets, tickets without an unconsumed Special Pass, and the `SPEED` variant SHALL be rejected without changing any pass.

#### Scenario: Ticket with Special Pass
- **GIVEN** SPECIAL attraction `A04` and ticket `T001`, valid today, with unconsumed Special Pass `PE01`
- **WHEN** `ATTRACTION ENTER;A04;T001` is processed
- **THEN** the ticket's user enters, `PE01` is marked as consumed and the output names `PE01`

#### Scenario: Ticket without usable Special Pass
- **GIVEN** ticket `T002` with no Special Pass, and ticket `T003` whose Special Pass is consumed
- **WHEN** either is used to enter `A04`
- **THEN** an error is shown and no one enters

#### Scenario: Pass not consumed when entry fails
- **GIVEN** SPECIAL attraction `A04` that is full, and ticket `T001` with unconsumed Special Pass `PE01`
- **WHEN** `ATTRACTION ENTER;A04;T001` is processed
- **THEN** an error is shown and `PE01` is still unconsumed

#### Scenario: SPEED not allowed at special attractions
- **WHEN** `ATTRACTION ENTER;A04;T001;SPEED` is processed
- **THEN** an error is shown, no one enters and no pass changes

#### Scenario: Consumed Special Pass cannot be reused
- **GIVEN** `T001` already entered `A04` with `PE01`, and has since left
- **WHEN** `ATTRACTION ENTER;A04;T001` is processed again
- **THEN** an error is shown
