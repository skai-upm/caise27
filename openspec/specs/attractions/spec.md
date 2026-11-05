# attractions Specification

## Purpose
Defines park attractions: their identity, name, maximum capacity and current occupants, how they are created, and how their occupants are consulted.

## Requirements

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

### Requirement: Occupants of an attraction
Each attraction SHALL hold references to the users currently inside it, never more than its capacity. Users SHALL NOT hold references to the attraction they are in.

#### Scenario: New attraction is empty
- **GIVEN** attraction `A02` was just created with capacity 12
- **WHEN** its occupants are inspected
- **THEN** the occupant collection is empty

### Requirement: Listing attraction occupants
The command `ATTRACTION LIST;AttractionIdentifier` SHALL display the e-mail addresses of the users currently inside the attraction. If it has no occupants, it SHALL produce a controlled empty result. The operation SHALL NOT change the attraction's state. An unknown identifier SHALL be reported as an error.

#### Scenario: List occupants
- **GIVEN** `ana@correo.com` and `pedro@correo.com` are inside `A01`
- **WHEN** `ATTRACTION LIST;A01` is processed
- **THEN** both e-mail addresses are displayed and the occupants are unchanged

#### Scenario: Empty attraction
- **GIVEN** attraction `A01` has no occupants
- **WHEN** `ATTRACTION LIST;A01` is processed
- **THEN** an empty result is displayed in a controlled way and `A01` still has no occupants

#### Scenario: Unknown attraction
- **WHEN** `ATTRACTION LIST;A99` is processed and `A99` does not exist
- **THEN** an error is shown

### Requirement: Attraction entry with an individual ticket
The command `ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier` SHALL admit the user associated with an existing individual ticket into an existing attraction. The ticket's date SHALL equal the current system date and the attraction SHALL have free capacity. Otherwise the entry SHALL be rejected with an error and occupants SHALL remain unchanged.

#### Scenario: Valid ticket and free capacity
- **GIVEN** the system date 2027-04-15, ticket `T001` for `ana@correo.com` dated 2027-04-15, and attraction `A01` with free capacity
- **WHEN** `ATTRACTION ENTER;A01;T001` is processed
- **THEN** a confirmation is shown and `ana@correo.com` is inside `A01`

#### Scenario: Ticket not valid today
- **GIVEN** ticket `T002` dated 2027-04-16 and the system date 2027-04-15
- **WHEN** `ATTRACTION ENTER;A01;T002` is processed
- **THEN** an error is shown and the occupants of `A01` are unchanged

#### Scenario: Attraction full
- **GIVEN** attraction `A02` whose occupants already equal its capacity
- **WHEN** a valid ticket is used to enter `A02`
- **THEN** an error is shown and no one enters

#### Scenario: Unknown ticket or attraction
- **WHEN** `ATTRACTION ENTER` names a ticket or attraction that does not exist
- **THEN** an error is shown and no occupants change

#### Scenario: User already inside
- **GIVEN** `ana@correo.com` is already inside `A01`
- **WHEN** another valid ticket of `ana@correo.com` is used to enter `A01`
- **THEN** an error is shown and `ana@correo.com` appears only once

### Requirement: Entry date checked only at entry
The ticket's date SHALL be checked only when entering. A user's presence in an attraction SHALL NOT depend on later changes to the system date.

#### Scenario: Date changes after entry
- **GIVEN** `ana@correo.com` entered `A01` with a ticket valid on 2027-04-15
- **WHEN** `SYSTEM DATE;2027-04-20` is processed
- **THEN** `ana@correo.com` is still inside `A01`

### Requirement: Attraction entry with a group ticket
`ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier` with a group ticket valid on the current system date SHALL admit together all the users of the tickets that make up the group ticket, using the same access operation as individual tickets. Group access SHALL be indivisible: if free capacity cannot hold all of them, or any of them is already inside, none SHALL enter.

#### Scenario: Whole group fits
- **GIVEN** group ticket `TG01` dated today for 3 members, and attraction `A01` with 3 or more free places
- **WHEN** `ATTRACTION ENTER;A01;TG01` is processed
- **THEN** all 3 members are inside `A01`

#### Scenario: Not enough capacity for the group
- **GIVEN** group ticket `TG01` dated today for 3 members, and attraction `A02` with 2 free places
- **WHEN** `ATTRACTION ENTER;A02;TG01` is processed
- **THEN** an error is shown and no member enters `A02`

#### Scenario: Group ticket not valid today
- **GIVEN** group ticket `TG02` dated after the current system date
- **WHEN** `ATTRACTION ENTER;A01;TG02` is processed
- **THEN** an error is shown and no one enters

### Requirement: Leaving an attraction
The command `ATTRACTION EXIT;AttractionIdentifier;Email` SHALL remove that user from the attraction's occupants and free their place. Users who entered with a group ticket SHALL leave individually with their own e-mail. An unknown attraction or a user who is not inside SHALL be reported as an error, leaving occupants unchanged.

#### Scenario: User leaves
- **GIVEN** `ana@correo.com` is inside `A01`
- **WHEN** `ATTRACTION EXIT;A01;ana@correo.com` is processed
- **THEN** `ana@correo.com` is no longer inside and one more place is available

#### Scenario: Group members leave one by one
- **GIVEN** the members of `TG01` entered `A01` together
- **WHEN** one member processes `ATTRACTION EXIT` with their e-mail
- **THEN** only that member leaves and the others remain inside

#### Scenario: User not inside
- **WHEN** `ATTRACTION EXIT;A01;luis@correo.com` is processed and that user is not inside `A01`
- **THEN** an error is shown and occupants are unchanged

### Requirement: Attraction status
The command `ATTRACTION STATUS;AttractionIdentifier` SHALL display the attraction identifier, name, maximum capacity, number of users currently inside, number of available places and the users currently inside. These SHALL be obtained from the attraction's own state, treating every occupant the same whatever ticket they entered with. Unknown attractions SHALL be reported as an error.

#### Scenario: Status after mixed entries
- **GIVEN** attraction `A01` "Dragon Rojo" with capacity 20, which `ana@correo.com` entered with an individual ticket and two members entered with a group ticket
- **WHEN** `ATTRACTION STATUS;A01` is processed
- **THEN** the output shows `A01`, `Dragon Rojo`, capacity 20, 3 inside, 17 available and the three e-mails

#### Scenario: Status reflects exits
- **GIVEN** the previous state
- **WHEN** `ATTRACTION EXIT;A01;ana@correo.com` and then `ATTRACTION STATUS;A01` are processed
- **THEN** the output shows 2 inside and 18 available

#### Scenario: Unknown attraction
- **WHEN** `ATTRACTION STATUS;A99` is processed
- **THEN** an error is shown

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

### Requirement: Access capabilities restrict attraction types
Ticket access capabilities SHALL be represented by interfaces implemented by the ticket types: a general capability implemented by every ticket, an individual capability implemented by individual tickets, and a group capability implemented by group tickets. Each attraction type SHALL be generic over the capability it accepts, with a bounded type parameter that decides which tickets its entry operations accept. INDIVIDUAL and SPECIAL SHALL share the individual capability; the Special Pass condition SHALL belong to the special attraction's behavior.

#### Scenario: Capability determines acceptance
- **GIVEN** each attraction type and each ticket kind
- **WHEN** acceptance is checked
- **THEN** acceptance matches the ticket implementing the capability that bounds the attraction's type parameter

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
