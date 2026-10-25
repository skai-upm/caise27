# Spec Delta

## ADDED Requirements

### Requirement: Attraction types
Attractions SHALL have one of the types GENERAL (admits individual and group tickets), INDIVIDUAL (only individual tickets), GROUP (only group tickets) and SPECIAL (only individual tickets, with Special Pass rules). Access capabilities SHALL be represented by interfaces, and attractions SHALL be generic types bounded by those interfaces so that each type only handles the ticket classes it admits.

#### Scenario: GROUP rejects individual tickets
- **WHEN** A02 is a GROUP attraction and `ATTRACTION ENTER;A02;T001` is processed with an individual ticket for today
- **THEN** the system reports an error and nobody enters

#### Scenario: INDIVIDUAL rejects group tickets
- **WHEN** A03 is an INDIVIDUAL attraction and `ATTRACTION ENTER;A03;TG01` is processed with a group ticket for today
- **THEN** the system reports an error and nobody enters

### Requirement: Typed attraction creation
`ATTRACTION CREATE;<id>;<nombre>;<capacidad>;<TIPO>` SHALL create an attraction of the given type (case-insensitive); the E2 syntax without a type SHALL keep working and create a GENERAL attraction. Unknown types SHALL be rejected.

#### Scenario: Create with and without type
- **WHEN** `ATTRACTION CREATE;A01;Tornado;20` and `ATTRACTION CREATE;A04;Castillo;10;SPECIAL` are processed
- **THEN** A01 is GENERAL and A04 is SPECIAL

#### Scenario: Unknown type
- **WHEN** `ATTRACTION CREATE;A05;Rio;10;WATER` is processed
- **THEN** the system reports an error

### Requirement: SpeedPass entry
`ATTRACTION ENTER;<atraccion>;<entrada>;SPEED` SHALL be accepted at GENERAL and INDIVIDUAL attractions for individual tickets and at GENERAL and GROUP attractions for group tickets. Every individual ticket admitted (the ticket itself, or each ticket of the group ticket) SHALL have an unused SpeedPass; after a successful entry exactly one SpeedPass per such ticket SHALL be consumed. If any ticket lacks one, or any other entry condition fails, nobody SHALL enter and no pass SHALL be consumed. The fourth parameter SHALL be exactly `SPEED`.

#### Scenario: Individual SPEED entry
- **WHEN** T001 has two unused SpeedPasses and `ATTRACTION ENTER;A01;T001;SPEED` succeeds
- **THEN** exactly one of them is consumed and the output names the consumed SpeedPass

#### Scenario: Group SPEED entry is atomic
- **WHEN** TG01 has three tickets and only two of them have unused SpeedPasses
- **THEN** `ATTRACTION ENTER;A01;TG01;SPEED` is rejected, nobody enters and no SpeedPass is consumed

#### Scenario: Group SPEED entry consumes one per member
- **WHEN** every ticket of TG01 has an unused SpeedPass and `ATTRACTION ENTER;A01;TG01;SPEED` succeeds
- **THEN** one SpeedPass of each ticket is consumed

### Requirement: Special attraction entry
SPECIAL attractions SHALL admit only individual tickets, SHALL require that the ticket has an unconsumed Special Pass, SHALL mark it consumed only after a successful entry, SHALL report the consumption, and SHALL reject the SPEED option.

#### Scenario: Special entry consumes the pass
- **WHEN** T001 has an unused Special Pass and `ATTRACTION ENTER;A04;T001` is processed at SPECIAL attraction A04
- **THEN** Ana enters, the Special Pass is consumed and the output says so

#### Scenario: Missing or used special pass
- **WHEN** T001 has no Special Pass, or it is already consumed
- **THEN** entry to A04 is rejected and nothing changes

#### Scenario: SPEED at special attraction
- **WHEN** `ATTRACTION ENTER;A04;T001;SPEED` is processed
- **THEN** the system reports an error and no pass is consumed

## MODIFIED Requirements

### Requirement: Attraction persistence
Attractions SHALL be stored with identifier, name, capacity and type and restored at startup; they SHALL always start empty. Stored attractions without a type SHALL be restored as GENERAL.

#### Scenario: Restart empties attractions
- **WHEN** users are inside A01 and the application restarts
- **THEN** A01 exists with the same name and capacity and nobody inside

#### Scenario: Type preserved
- **WHEN** SPECIAL attraction A04 exists and the application restarts
- **THEN** A04 is still SPECIAL
