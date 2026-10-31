# Spec Delta

## MODIFIED Requirements

### Requirement: ATTRACTION CREATE command
`ATTRACTION CREATE;<id>;<name>;<capacity>` SHALL create a `GENERAL` attraction with a unique identifier, a name, a positive integer maximum capacity and no users present; `ATTRACTION CREATE;<id>;<name>;<capacity>;<type>` SHALL create an attraction of type `GENERAL`, `INDIVIDUAL`, `GROUP` or `SPECIAL`. Duplicate identifiers, blank values, non-positive or non-numeric capacities and unknown types SHALL be rejected.

#### Scenario: Create attraction
- **WHEN** `ATTRACTION CREATE;A01;Tornado;20` is executed and no attraction `A01` exists
- **THEN** attraction `A01` named `Tornado` with capacity 20, type `GENERAL` and no users exists

#### Scenario: Invalid capacity or duplicate
- **WHEN** `ATTRACTION CREATE;A02;Noria;0`, `ATTRACTION CREATE;A02;Noria;diez` or a second `ATTRACTION CREATE;A01;...` is executed
- **THEN** an error is reported and no attraction is created or changed

#### Scenario: Typed creation
- **WHEN** `ATTRACTION CREATE;A03;Casa;5;SPECIAL` is executed
- **THEN** attraction `A03` has type `SPECIAL`; `ATTRACTION CREATE;A04;X;5;VIP` is rejected

### Requirement: ATTRACTION ENTER command
`ATTRACTION ENTER;<attractionId>;<ticketId>[;SPEED]` SHALL admit the users of the ticket: the ticket's user for an individual ticket, or every constituent's user for a group ticket. Entry SHALL be rejected when the attraction or ticket does not exist, the attraction type does not admit the ticket kind, the ticket date differs from the system date, any of those users is already present, capacity would be exceeded, or a required pass is missing. Entry, and any pass consumption, SHALL be all-or-nothing.

#### Scenario: Individual entry
- **WHEN** ticket `T001` for Ana is dated on the system date and `ATTRACTION ENTER;A01;T001` is executed
- **THEN** Ana is present in `A01`

#### Scenario: Group entry
- **WHEN** group ticket `TG01` for Ana and Pedro is dated on the system date and `ATTRACTION ENTER;A01;TG01` is executed
- **THEN** Ana and Pedro are present in `A01`

#### Scenario: Atomic capacity rejection
- **WHEN** `A01` has three places left and a valid group ticket for four users is used
- **THEN** an error is reported, nobody enters and occupancy is unchanged

#### Scenario: Ticket not for today
- **WHEN** a ticket dated a different day than the system date is used
- **THEN** entry is rejected

### Requirement: Attraction persistence
Attraction definitions (identifier, name, capacity, type) SHALL be persisted and restored at startup; definitions stored without a type SHALL load as `GENERAL`. Users present SHALL NOT be persisted: every attraction SHALL be empty when the application starts.

#### Scenario: Restart empties attractions
- **WHEN** Ana is present in `A01` and the application restarts
- **THEN** `A01` exists with its name, capacity and type and no users present

#### Scenario: Legacy definition
- **WHEN** `attractions.txt` contains the E2 line `A01;Tornado;20`
- **THEN** `A01` loads as a `GENERAL` attraction

## ADDED Requirements

### Requirement: Attraction types
`GENERAL` attractions SHALL admit individual and group tickets, `INDIVIDUAL` only individual tickets, `GROUP` only group tickets, and `SPECIAL` only individual tickets plus its Special Pass rule. Ticket access capabilities SHALL be modelled with interfaces and attractions SHALL use bounded generic types to restrict the ticket kinds they handle.

#### Scenario: Type compatibility
- **WHEN** a group ticket is used at an `INDIVIDUAL` or `SPECIAL` attraction, or an individual ticket at a `GROUP` attraction
- **THEN** entry is rejected and nobody enters

### Requirement: SpeedPass entry
With the `SPEED` option, an individual ticket at a `GENERAL` or `INDIVIDUAL` attraction SHALL consume exactly one unused SpeedPass of that ticket, and a group ticket at a `GENERAL` or `GROUP` attraction SHALL consume one unused SpeedPass of every constituent; if any constituent lacks one, or any other condition fails, nobody SHALL enter and no pass SHALL be consumed. The output SHALL report each consumed pass.

#### Scenario: Individual SPEED entry
- **WHEN** `T001` has two unused SpeedPasses and `ATTRACTION ENTER;A01;T001;SPEED` succeeds
- **THEN** exactly one SpeedPass is consumed and its identifier is reported

#### Scenario: Atomic group SPEED entry
- **WHEN** one constituent of `TG01` has no unused SpeedPass and `ATTRACTION ENTER;A01;TG01;SPEED` is executed
- **THEN** an error is reported, nobody enters and no SpeedPass is consumed

### Requirement: Special Pass entry
At a `SPECIAL` attraction an individual ticket SHALL enter only if it has an unused Special Pass, which SHALL be consumed after successful entry and reported. The `SPEED` option SHALL be rejected at `SPECIAL` attractions.

#### Scenario: Special Pass consumed
- **WHEN** `T001` has an unused Special Pass and `ATTRACTION ENTER;A03;T001` succeeds at SPECIAL attraction `A03`
- **THEN** the Special Pass becomes consumed and is reported

#### Scenario: Missing pass or SPEED
- **WHEN** the ticket has no unused Special Pass, or `ATTRACTION ENTER;A03;T001;SPEED` is executed
- **THEN** entry is rejected and no pass is consumed
