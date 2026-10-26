# Spec Delta

## MODIFIED Requirements

### Requirement: Enter with a ticket
`ATTRACTION ENTER;<attractionId>;<ticketId>` SHALL add to the attraction the user of an individual ticket, or the users of all individual tickets composing a group ticket, and print `OK ATTRACTION ENTERED -> <n> users`, followed by the passes consumed, if any. An optional fourth field `SPEED` SHALL request SpeedPass use; any other fourth field SHALL be rejected. Both ticket kinds SHALL be handled through the same entry rules.

#### Scenario: Individual entry
- **WHEN** the system date is 2026-10-15 and `ATTRACTION ENTER;A01;T001` is processed for Ana's ticket dated 2026-10-15
- **THEN** Ana is present in `A01`

#### Scenario: Group entry
- **WHEN** `ATTRACTION ENTER;A01;TG01` is processed for a group ticket of two members dated today
- **THEN** both members are present in `A01`

## ADDED Requirements

### Requirement: Entry with SpeedPasses
With `SPEED`, an individual ticket SHALL consume exactly one unused SpeedPass of that ticket and a group ticket one unused SpeedPass of each individual ticket composing it, only when the entry succeeds. If any ticket involved has no unused SpeedPass, or any other entry condition fails, nobody SHALL enter and no pass SHALL be consumed. The response SHALL name the consumed SpeedPasses.

#### Scenario: Individual SPEED entry
- **WHEN** `T001` has two unused SpeedPasses and `ATTRACTION ENTER;A01;T001;SPEED` is processed at a `GENERAL` attraction
- **THEN** Ana enters, exactly one SpeedPass becomes consumed and the response names it

#### Scenario: Group SPEED entry is atomic
- **WHEN** `TG01` has three members and only two of their tickets have an unused SpeedPass
- **THEN** `ATTRACTION ENTER;A01;TG01;SPEED` is rejected, nobody enters and no SpeedPass is consumed

#### Scenario: Failed entry consumes nothing
- **WHEN** `ATTRACTION ENTER;A01;T001;SPEED` fails because the attraction is full
- **THEN** the SpeedPasses of `T001` keep their status

### Requirement: Special attraction entry
Entry to a `SPECIAL` attraction SHALL require the individual ticket's Special Pass to exist and be unused, and SHALL mark it consumed only after a successful entry, reporting it in the response. `SPEED` SHALL be rejected at `SPECIAL` attractions with an `ERROR` line.

#### Scenario: Special Pass consumed
- **WHEN** `T001` has an unused Special Pass and `ATTRACTION ENTER;A06;T001` is processed at `SPECIAL` attraction `A06`
- **THEN** Ana enters and the response reports `T001-SPECIAL` as consumed

#### Scenario: No usable Special Pass
- **WHEN** `T001`'s Special Pass is consumed or it has none
- **THEN** `ATTRACTION ENTER;A06;T001` is rejected and nobody enters

#### Scenario: SPEED at a special attraction
- **WHEN** `ATTRACTION ENTER;A06;T001;SPEED` is processed
- **THEN** an `ERROR` line is printed and nothing is consumed
