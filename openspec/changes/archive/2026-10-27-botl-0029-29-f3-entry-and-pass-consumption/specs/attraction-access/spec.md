# Spec Delta

## ADDED Requirements

### Requirement: Entry with a SpeedPass
`ATTRACTION ENTER;<attraction>;<ticket>;SPEED` with an individual ticket at a GENERAL or INDIVIDUAL attraction SHALL require at least one unconsumed SpeedPass of that ticket, and on successful entry SHALL mark exactly one of them as consumed and state which one in the output; if access fails, no SpeedPass SHALL change.

#### Scenario: SpeedPass consumed
- **WHEN** `T001` has unconsumed SpeedPasses `SP01:1` and `SP02:1` and `ATTRACTION ENTER;A01;T001;SPEED` succeeds
- **THEN** exactly one of them is consumed and the output names it

#### Scenario: No SpeedPass available
- **WHEN** `T001` has no unconsumed SpeedPass and `ATTRACTION ENTER;A01;T001;SPEED` is processed
- **THEN** an error is reported and Ana does not enter

#### Scenario: Failure leaves passes untouched
- **WHEN** `T001` has an unconsumed SpeedPass but the attraction is full
- **THEN** the entry is rejected and the SpeedPass remains unconsumed

### Requirement: Group entry with SpeedPasses
`ATTRACTION ENTER;<attraction>;<groupTicket>;SPEED` at GENERAL or GROUP attractions SHALL check, before changing any state, that the group ticket is valid today, the attraction accepts group tickets, there is room for all members and every individual ticket of the composition has an unconsumed SpeedPass; on success all members enter and exactly one SpeedPass per component is consumed, otherwise nobody enters and nothing is consumed.

#### Scenario: All members have SpeedPasses
- **WHEN** both components of `TG01` have an unconsumed SpeedPass and `ATTRACTION ENTER;A03;TG01;SPEED` is processed
- **THEN** both members enter and one SpeedPass of each component is consumed

#### Scenario: One member lacks a SpeedPass
- **WHEN** only one component of `TG01` has a SpeedPass
- **THEN** the entry is rejected, nobody enters and no SpeedPass is consumed

### Requirement: Special attraction entry
At a SPECIAL attraction, `ATTRACTION ENTER;<attraction>;<individualTicket>` SHALL automatically find the ticket's Special Pass and require it to be unconsumed; the pass SHALL be marked as consumed only when the entry succeeds and the output SHALL say so. The `SPEED` variant and group tickets SHALL be rejected at SPECIAL attractions.

#### Scenario: Entry consumes the Special Pass
- **WHEN** `T001` has unconsumed Special Pass `PE01` and `ATTRACTION ENTER;A04;T001` is processed
- **THEN** Ana enters, `PE01` is consumed and the output states that the Special Pass was consumed

#### Scenario: Missing or consumed Special Pass
- **WHEN** `T001` has no Special Pass, or it is already consumed
- **THEN** the entry is rejected and the pass state does not change

#### Scenario: SPEED at a special attraction
- **WHEN** `ATTRACTION ENTER;A04;T001;SPEED` is processed
- **THEN** an error is reported and nothing changes
