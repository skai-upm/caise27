# Spec Delta

## ADDED Requirements

### Requirement: Entry with SpeedPasses
`ATTRACTION ENTER;<IdAtraccion>;<IdTicket>;SPEED` SHALL, at attractions that allow it, require an unused SpeedPass for every individual ticket the credential represents (the ticket itself, or each individual ticket of a group ticket) and, only if the entry succeeds under all other rules, consume exactly one unused SpeedPass per such ticket and name the consumed passes in the output; a third parameter other than `SPEED` SHALL be rejected.

#### Scenario: Individual SPEED entry consumes one pass
- **GIVEN** ticket `T001` valid today with unused SpeedPasses `SP01-1` … `SP01-10`
- **WHEN** `ATTRACTION ENTER;A01;T001;SPEED` is processed at GENERAL attraction `A01`
- **THEN** the holder enters, exactly one SpeedPass is consumed and the output names it

#### Scenario: Group SPEED entry is all-or-nothing
- **GIVEN** group ticket `TG01` valid today where one constituent ticket has no unused SpeedPass
- **WHEN** `ATTRACTION ENTER;A01;TG01;SPEED` is processed
- **THEN** it is rejected, no member enters and no SpeedPass is consumed

#### Scenario: Group SPEED entry consumes one per member
- **GIVEN** every constituent ticket of `TG01` has an unused SpeedPass
- **WHEN** `ATTRACTION ENTER;A01;TG01;SPEED` succeeds
- **THEN** one SpeedPass per constituent ticket is consumed and named in the output

#### Scenario: Other failure consumes nothing
- **GIVEN** the tickets have SpeedPasses but the attraction lacks capacity, the date does not match, or the ticket kind is not admitted
- **WHEN** entry with `SPEED` is requested
- **THEN** nobody enters and no pass is consumed

#### Scenario: Missing SpeedPass
- **WHEN** `ATTRACTION ENTER;A01;T002;SPEED` is processed for a ticket without unused SpeedPasses
- **THEN** it is rejected and nobody enters

### Requirement: SPECIAL entry with Special Pass
At a SPECIAL attraction, entry SHALL require that the individual ticket has an unused Special Pass, SHALL consume it automatically only when the entry succeeds and name it in the output, and SHALL reject the `SPEED` option without changing any pass.

#### Scenario: Special Pass consumed automatically
- **GIVEN** SPECIAL attraction `A04` and ticket `T001` valid today with unused Special Pass `SPC1`
- **WHEN** `ATTRACTION ENTER;A04;T001` is processed
- **THEN** the holder enters and `SPC1` is consumed and reported

#### Scenario: No usable Special Pass
- **WHEN** the ticket has no Special Pass or it is already consumed
- **THEN** entry to `A04` is rejected

#### Scenario: SPEED rejected at SPECIAL
- **WHEN** `ATTRACTION ENTER;A04;T001;SPEED` is processed
- **THEN** it is rejected and all pass states are unchanged

### Requirement: Normal entry unchanged
`ATTRACTION ENTER;<IdAtraccion>;<IdTicket>` without `SPEED` at non-SPECIAL attractions SHALL behave as before and SHALL NOT consume any pass.

#### Scenario: Normal entry keeps passes
- **GIVEN** `T001` has unused SpeedPasses
- **WHEN** `ATTRACTION ENTER;A01;T001` succeeds
- **THEN** no SpeedPass is consumed
