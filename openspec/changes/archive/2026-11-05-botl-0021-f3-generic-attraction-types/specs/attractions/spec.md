# Spec Delta

## MODIFIED Requirements

### Requirement: Attraction creation
The command `ATTRACTION CREATE;Identifier;Name;Capacity[;Type]` SHALL create an attraction of the given type (`GENERAL`, `INDIVIDUAL`, `GROUP` or `SPECIAL`; `GENERAL` when omitted, for Delivery 2 compatibility) with that unique identifier, a non-empty name and a maximum capacity given as a positive whole number. The attraction SHALL start with no occupants. A duplicate identifier, empty field, non-positive or non-numeric capacity, or unknown type SHALL be rejected with an error, and no attraction SHALL be created or changed.

#### Scenario: Create attractions
- **WHEN** `ATTRACTION CREATE;A01;Dragon Rojo;20` is processed
- **THEN** a confirmation is shown, and attraction `A01` named `Dragon Rojo` exists with capacity 20 and no occupants

#### Scenario: Duplicate identifier rejected
- **GIVEN** attraction `A01` exists named `Dragon Rojo`
- **WHEN** `ATTRACTION CREATE;A01;Otra;5` is processed
- **THEN** an error is shown and `A01` keeps its name and capacity

#### Scenario: Invalid capacity rejected
- **WHEN** `ATTRACTION CREATE;A03;Noria;0`, `ATTRACTION CREATE;A03;Noria;-2`, `ATTRACTION CREATE;A03;Noria;diez` or `ATTRACTION CREATE;A03;Noria` is processed
- **THEN** an error is shown and no attraction `A03` exists

#### Scenario: Typed attractions
- **WHEN** `ATTRACTION CREATE;A02;Montana Rusa Individual;12;INDIVIDUAL`, `ATTRACTION CREATE;A03;Rafting Familiar;24;GROUP` and `ATTRACTION CREATE;A04;Pasaje del Terror;15;SPECIAL` are processed
- **THEN** the attractions exist with types INDIVIDUAL, GROUP and SPECIAL

#### Scenario: Default type and unknown type
- **WHEN** `ATTRACTION CREATE;A01;Dragon Rojo;20` is processed, and then `ATTRACTION CREATE;A05;Noria;10;VIP`
- **THEN** `A01` is GENERAL, and an error is shown with no `A05` created

## ADDED Requirements

### Requirement: Ticket kinds accepted by attraction type
Entry SHALL be allowed only with ticket kinds the attraction's type accepts: GENERAL accepts individual and group tickets, INDIVIDUAL accepts individual tickets only, GROUP accepts group tickets only, and SPECIAL accepts individual tickets only. Any other kind SHALL be rejected with an error and no one SHALL enter. All other entry rules (validity date, capacity, all-or-nothing) still apply. `ATTRACTION STATUS` SHALL also show the attraction type.

#### Scenario: General accepts both kinds
- **GIVEN** GENERAL attraction `A01`, individual ticket `T001` and group ticket `TG01`, both valid today
- **WHEN** both are used to enter `A01`
- **THEN** their users enter

#### Scenario: Individual rejects group tickets
- **GIVEN** INDIVIDUAL attraction `A02` and group ticket `TG01` valid today
- **WHEN** `ATTRACTION ENTER;A02;TG01` is processed
- **THEN** an error is shown and no member enters

#### Scenario: Group rejects individual tickets
- **GIVEN** GROUP attraction `A03` and individual ticket `T001` valid today
- **WHEN** `ATTRACTION ENTER;A03;T001` is processed
- **THEN** an error is shown and no one enters

### Requirement: Special attractions require a Special Pass
A SPECIAL attraction SHALL admit an individual ticket only if that ticket has an unconsumed Special Pass. The pass is used automatically, with no extra option in the entry command. Group tickets and tickets without an unconsumed Special Pass SHALL be rejected.

#### Scenario: Ticket with Special Pass
- **GIVEN** SPECIAL attraction `A04` and ticket `T001`, valid today, with unconsumed Special Pass `PE01`
- **WHEN** `ATTRACTION ENTER;A04;T001` is processed
- **THEN** the ticket's user enters

#### Scenario: Ticket without usable Special Pass
- **GIVEN** ticket `T002` with no Special Pass, and ticket `T003` whose Special Pass is consumed
- **WHEN** either is used to enter `A04`
- **THEN** an error is shown and no one enters

### Requirement: Access capabilities restrict attraction types
Ticket access capabilities SHALL be represented by interfaces implemented by the ticket types: a general capability implemented by every ticket, an individual capability implemented by individual tickets, and a group capability implemented by group tickets. Each attraction type SHALL be generic over the capability it accepts, with a bounded type parameter that decides which tickets its entry operations accept. INDIVIDUAL and SPECIAL SHALL share the individual capability; the Special Pass condition SHALL belong to the special attraction's behavior.

#### Scenario: Capability determines acceptance
- **GIVEN** each attraction type and each ticket kind
- **WHEN** acceptance is checked
- **THEN** acceptance matches the ticket implementing the capability that bounds the attraction's type parameter
