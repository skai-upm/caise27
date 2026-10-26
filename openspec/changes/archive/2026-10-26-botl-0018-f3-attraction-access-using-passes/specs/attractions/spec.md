# Spec Delta

## ADDED Requirements

### Requirement: Entry with SpeedPasses
`ATTRACTION ENTER;<attractionId>;<ticketId>;SPEED` SHALL request SpeedPass use. It is allowed at GENERAL and INDIVIDUAL attractions for individual tickets and at GENERAL and GROUP attractions for group tickets. An individual entry with SPEED SHALL consume exactly one unused SpeedPass of the ticket (`ERROR NO SPEEDPASS AVAILABLE` if it has none) and respond `OK ENTERED 1 USERS (SPEEDPASS <passId> CONSUMED)`. Any fourth parameter other than `SPEED` gives `ERROR INVALID OPTION`.

#### Scenario: Individual SPEED entry
- **WHEN** T001 valid today has unused SpeedPasses T001-S1 and T001-S2 and `ATTRACTION ENTER;A01;T001;SPEED` is processed at a GENERAL attraction
- **THEN** the response is `OK ENTERED 1 USERS (SPEEDPASS T001-S1 CONSUMED)`, T001-S1 is consumed and T001-S2 is not

#### Scenario: No SpeedPass left
- **WHEN** T001 has no unused SpeedPass and `ATTRACTION ENTER;A01;T001;SPEED` is processed
- **THEN** the response is `ERROR NO SPEEDPASS AVAILABLE` and nobody enters

#### Scenario: Normal entry unchanged
- **WHEN** `ATTRACTION ENTER;A01;T001` is processed for a ticket with SpeedPasses
- **THEN** the user enters and no pass is consumed

### Requirement: Group entry with SpeedPasses
A group ticket entering with SPEED SHALL require every individual ticket of its composition to have at least one unused SpeedPass, and SHALL consume exactly one per member, responding `OK ENTERED <n> USERS (<n> SPEEDPASSES CONSUMED)`. The operation is atomic: if any member lacks a SpeedPass (`ERROR NO SPEEDPASS AVAILABLE`) or any other access condition fails, no user enters and no SpeedPass is consumed.

#### Scenario: All members have passes
- **WHEN** both tickets of TG01 have an unused SpeedPass and `ATTRACTION ENTER;A01;TG01;SPEED` is processed at a GENERAL attraction
- **THEN** the response is `OK ENTERED 2 USERS (2 SPEEDPASSES CONSUMED)`

#### Scenario: One member without pass
- **WHEN** only the first ticket of TG01 has a SpeedPass and `ATTRACTION ENTER;A01;TG01;SPEED` is processed
- **THEN** the response is `ERROR NO SPEEDPASS AVAILABLE`, nobody enters and the first ticket's SpeedPass is still unused

#### Scenario: Capacity failure consumes nothing
- **WHEN** every member has a SpeedPass but the attraction lacks capacity for the group
- **THEN** the response is `ERROR CAPACITY EXCEEDED` and no SpeedPass is consumed

### Requirement: Special attraction entry
SPECIAL attractions SHALL admit only individual tickets, SHALL reject the SPEED option with `ERROR SPEED NOT ALLOWED`, and SHALL automatically use the ticket's Special Pass: a ticket without one gets `ERROR NO SPECIAL PASS`, one whose Special Pass is already consumed gets `ERROR SPECIAL PASS CONSUMED`. The Special Pass is marked consumed only after a successful entry, and the response is `OK ENTERED 1 USERS (SPECIAL PASS <passId> CONSUMED)`.

#### Scenario: Special Pass consumed on entry
- **WHEN** T001 valid today has an unused Special Pass and `ATTRACTION ENTER;A04;T001` is processed at a SPECIAL attraction
- **THEN** the response is `OK ENTERED 1 USERS (SPECIAL PASS T001-SPECIAL CONSUMED)` and the pass is consumed

#### Scenario: Second use rejected
- **WHEN** after leaving, `ATTRACTION ENTER;A04;T001` is processed again
- **THEN** the response is `ERROR SPECIAL PASS CONSUMED`

#### Scenario: SPEED at special attraction
- **WHEN** `ATTRACTION ENTER;A04;T001;SPEED` is processed
- **THEN** the response is `ERROR SPEED NOT ALLOWED` and no pass is consumed

#### Scenario: Failed special entry keeps the pass
- **WHEN** the SPECIAL attraction is full and `ATTRACTION ENTER;A04;T001` is processed
- **THEN** the response is `ERROR CAPACITY EXCEEDED` and the Special Pass stays unused
