# Spec Delta

## MODIFIED Requirements

### Requirement: Create attraction command
`ATTRACTION CREATE;IdAtraccion;Nombre;Capacidad[;Tipo]` SHALL create an attraction with a unique identifier, a nonempty name, a positive integer maximum capacity, a type among `GENERAL`, `INDIVIDUAL`, `GROUP` and `SPECIAL` (default `GENERAL` when omitted) and no users present. Invalid values, an unknown type or a duplicate identifier SHALL be rejected.

#### Scenario: Create attraction
- **WHEN** `ATTRACTION CREATE;A01;Tornado;20` is executed
- **THEN** attraction `A01` named `Tornado` with capacity 20, type `GENERAL` and no users present exists

#### Scenario: Invalid attraction
- **WHEN** `ATTRACTION CREATE` uses an existing identifier or a capacity that is not a positive integer
- **THEN** the command is rejected

#### Scenario: Typed attraction
- **WHEN** `ATTRACTION CREATE;A02;Casa;10;SPECIAL` is executed
- **THEN** attraction `A02` of type `SPECIAL` exists; `ATTRACTION CREATE;A03;X;10;VIP` is rejected

### Requirement: Enter attraction command
`ATTRACTION ENTER;IdAtraccion;IdTicket[;SPEED]` SHALL admit all users of the given ticket (one user for an individual ticket, every constituent user for a group ticket) only if the attraction type accepts that ticket kind, the ticket is dated on the system date, none of those users is already inside, the attraction has room for all of them, and every pass requirement is met. Otherwise nobody SHALL enter and no pass SHALL be consumed.

#### Scenario: Individual entry
- **GIVEN** attraction `A01` with free places and ticket `T001` dated on the system date
- **WHEN** `ATTRACTION ENTER;A01;T001` is executed
- **THEN** the ticket's user is present in `A01`

#### Scenario: Atomic capacity rejection
- **GIVEN** an attraction with three remaining places and a valid group ticket for four people
- **WHEN** the group ticket is used to enter
- **THEN** nobody enters and occupancy is unchanged

#### Scenario: Ticket not for today
- **WHEN** a ticket dated on a different day than the system date is used to enter
- **THEN** entry is rejected

### Requirement: Attraction persistence and runtime occupancy
Attraction definitions, including their type, SHALL be persisted and restored at startup; definitions stored without a type SHALL load as `GENERAL`. The users present SHALL NOT be persisted, so every attraction starts empty.

#### Scenario: Attractions start empty
- **GIVEN** attraction `A01` had users present when the application ended
- **WHEN** the application restarts
- **THEN** `A01` exists with its name and capacity and no users present

#### Scenario: Type preserved
- **GIVEN** a `SPECIAL` attraction
- **WHEN** the application restarts
- **THEN** it is still `SPECIAL`

## ADDED Requirements

### Requirement: Ticket kinds admitted by attraction type
`GENERAL` attractions SHALL admit individual and group tickets, `INDIVIDUAL` only individual tickets, `GROUP` only group tickets and `SPECIAL` only individual tickets. Ticket access capabilities SHALL be modelled with interfaces, and attractions SHALL use bounded generic types that restrict the ticket kinds they handle.

#### Scenario: Incompatible ticket kinds
- **WHEN** a group ticket is used at an `INDIVIDUAL` or `SPECIAL` attraction, or an individual ticket at a `GROUP` attraction
- **THEN** entry is rejected

### Requirement: SpeedPass entry
With `SPEED`, an individual ticket at a `GENERAL` or `INDIVIDUAL` attraction SHALL consume exactly one unused SpeedPass of that ticket, and a group ticket at a `GENERAL` or `GROUP` attraction SHALL consume one unused SpeedPass of every constituent ticket. If any ticket lacks an unused SpeedPass or any other condition fails, nobody SHALL enter and no pass SHALL be consumed. The reply SHALL report the consumed pass identifiers.

#### Scenario: Individual SPEED entry
- **GIVEN** ticket `T001` with two unused SpeedPasses at a `GENERAL` attraction
- **WHEN** `ATTRACTION ENTER;A01;T001;SPEED` succeeds
- **THEN** exactly one SpeedPass becomes consumed and its identifier is reported

#### Scenario: Group SPEED entry is atomic
- **GIVEN** a group ticket where one constituent ticket has no unused SpeedPass
- **WHEN** it enters with `SPEED`
- **THEN** no member enters and no SpeedPass is consumed

### Requirement: Special attraction entry
A `SPECIAL` attraction SHALL admit an individual ticket only if it has an unused Special Pass, SHALL mark that pass consumed only after successful entry and report it, and SHALL reject the `SPEED` option.

#### Scenario: Special Pass consumed
- **GIVEN** ticket `T001` with an unused Special Pass
- **WHEN** it enters a `SPECIAL` attraction
- **THEN** the user enters, the Special Pass becomes consumed and the reply reports it

#### Scenario: SPEED rejected at SPECIAL
- **WHEN** `ATTRACTION ENTER` with `SPEED` targets a `SPECIAL` attraction
- **THEN** entry is rejected and no pass is consumed
