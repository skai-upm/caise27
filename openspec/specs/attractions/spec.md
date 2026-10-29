# attractions Specification

## Purpose

Defines park attractions: their identity, name, maximum capacity and the collection of users currently inside them, plus creation and the read-only listing of occupants.

## Requirements

### Requirement: Create attraction
The command `ATTRACTION CREATE;Identifier;Name;Capacity` SHALL create an attraction with a unique identifier, a name, a maximum capacity and an initially empty collection of users inside it. It SHALL be rejected with an error message when the identifier already exists, the identifier or name is blank, the capacity is not a whole number greater than zero, or the number of arguments is wrong.

#### Scenario: Create attractions from the source example
- **WHEN** `ATTRACTION CREATE;A01;Dragon Rojo;20` and `ATTRACTION CREATE;A02;Torre del Cielo;12` are processed
- **THEN** both are confirmed, `A01` has name `Dragon Rojo` and capacity 20, `A02` has capacity 12, and both are empty

#### Scenario: Duplicate identifier rejected
- **WHEN** `A01` exists and `ATTRACTION CREATE;A01;Otra;5` is processed
- **THEN** an error message is shown and `A01` is unchanged

#### Scenario: Invalid capacity rejected
- **WHEN** `ATTRACTION CREATE;A03;X;0`, `ATTRACTION CREATE;A03;X;-1` or `ATTRACTION CREATE;A03;X;abc` is processed
- **THEN** an error message is shown and `A03` is not created

### Requirement: Attraction occupants
An attraction SHALL hold references to the users currently inside it and SHALL never hold more users than its capacity or the same user twice; users SHALL NOT hold any reference to the attraction they are in.

#### Scenario: Capacity bound respected
- **WHEN** an attraction with capacity 1 already holds one user and another user is added
- **THEN** the addition is refused and the attraction still holds one user

### Requirement: List attraction occupants
The command `ATTRACTION LIST;AttractionIdentifier` SHALL display the email addresses of the users currently inside the attraction, or a controlled empty result when there are none, without changing the attraction's state. An unknown attraction SHALL be rejected with an error message.

#### Scenario: List occupants
- **WHEN** `ana@correo.com` and `pedro@correo.com` are inside `A01` and `ATTRACTION LIST;A01` is processed
- **THEN** both email addresses are displayed and `A01` still holds both users

#### Scenario: Empty attraction
- **WHEN** `A02` has no users inside and `ATTRACTION LIST;A02` is processed
- **THEN** an empty-result message is displayed and no error occurs

#### Scenario: Unknown attraction
- **WHEN** `ATTRACTION LIST;A99` is processed and no attraction `A99` exists
- **THEN** an error message is shown

### Requirement: Enter attraction with an individual ticket
The command `ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier` SHALL make the user of the given individual ticket enter the attraction when the attraction exists, the ticket exists, the ticket's date equals the current system date, the attraction has room for the user, and the user is not already inside it. Otherwise it SHALL be rejected with an error message and no user SHALL enter. The ticket date SHALL be checked only at entry.

#### Scenario: Enter with today's ticket
- **WHEN** the current date is 2027-04-15, `T001` is an individual ticket of `ana@correo.com` dated 2027-04-15, and `ATTRACTION ENTER;A01;T001` is processed
- **THEN** a confirmation is shown and `ATTRACTION LIST;A01` includes `ana@correo.com`

#### Scenario: Ticket for another date rejected
- **WHEN** the current date is 2027-04-14 (or 2027-04-16) and `ATTRACTION ENTER;A01;T001` is processed for a ticket dated 2027-04-15
- **THEN** an error message is shown and nobody enters

#### Scenario: Full attraction rejected
- **WHEN** attraction `A03` has capacity 1 and already holds one user, and another valid ticket is used to enter it
- **THEN** an error message is shown and the attraction still holds one user

#### Scenario: Unknown attraction or ticket rejected
- **WHEN** `ATTRACTION ENTER;A99;T001` or `ATTRACTION ENTER;A01;T999` is processed and the attraction or ticket does not exist
- **THEN** an error message is shown

#### Scenario: Presence independent of later date changes
- **WHEN** `ana@correo.com` entered `A01` on 2027-04-15 and `SYSTEM DATE;2027-04-16` is then processed
- **THEN** `ana@correo.com` is still inside `A01`

### Requirement: Access through the common ticket abstraction
Attraction access SHALL be decided through the abstraction common to all ticket types, each ticket type providing the users it admits, rather than through separate access mechanisms per ticket type; the attraction SHALL have room for every admitted user or none SHALL enter.

#### Scenario: Users admitted by an individual ticket
- **WHEN** access is requested with an individual ticket
- **THEN** exactly the ticket's user is admitted

#### Scenario: Users admitted by a group ticket
- **WHEN** access is requested with a group ticket
- **THEN** exactly the users of its composition are admitted, through the same operation used for individual tickets

### Requirement: Enter attraction with a group ticket
Using `ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier` with a group ticket valid on the current system date SHALL make all users of the individual tickets in its composition enter together. Access SHALL be indivisible: if the attraction does not have room for all of them, or any of them is already inside, none SHALL enter and an error message SHALL be shown.

#### Scenario: Group enters together
- **WHEN** group ticket `TG01` for 2027-04-15 contains `ana@correo.com` and `pedro@correo.com`, the current date is 2027-04-15, and `ATTRACTION ENTER;A01;TG01` is processed
- **THEN** both users are inside `A01`

#### Scenario: Insufficient capacity, nobody enters
- **WHEN** attraction `A03` has 1 free place and a group ticket with 2 users is used to enter it
- **THEN** an error message is shown and neither user enters

#### Scenario: Group ticket for another date rejected
- **WHEN** the group ticket is dated 2027-04-15 and the current date is 2027-04-16
- **THEN** an error message is shown and nobody enters

### Requirement: Exit attraction
The command `ATTRACTION EXIT;AttractionIdentifier;Email` SHALL remove the user with that email from the attraction's occupants and release the corresponding place. Each user who entered with a group ticket SHALL be able to leave individually. An unknown attraction or a user who is not inside SHALL be rejected with an error message.

#### Scenario: One group member leaves
- **WHEN** `ana@correo.com` and `pedro@correo.com` entered `A01` with a group ticket and `ATTRACTION EXIT;A01;ana@correo.com` is processed
- **THEN** only `pedro@correo.com` remains inside and one more place is available

#### Scenario: User not inside rejected
- **WHEN** `luis@correo.com` is not inside `A01` and `ATTRACTION EXIT;A01;luis@correo.com` is processed
- **THEN** an error message is shown and the occupants are unchanged

### Requirement: Attraction status
The command `ATTRACTION STATUS;AttractionIdentifier` SHALL display the attraction identifier, name, maximum capacity, number of users currently inside, number of available places and the users currently inside, computed from the attraction's own state and independent of how users entered. An unknown attraction SHALL be rejected with an error message.

#### Scenario: Status after mixed entries and an exit
- **WHEN** `A01` (capacity 20) holds `pedro@correo.com` (group ticket) and `luis@correo.com` (individual ticket) and `ATTRACTION STATUS;A01` is processed
- **THEN** the output shows `A01`, `Dragon Rojo`, capacity 20, 2 users inside, 18 available places, and both email addresses

### Requirement: Attraction types
Every attraction SHALL have one of the types GENERAL, INDIVIDUAL, GROUP or SPECIAL. `ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>` SHALL create an attraction of the given type; `ATTRACTION CREATE;<identifier>;<name>;<capacity>` SHALL create a GENERAL attraction; an unknown type SHALL be rejected with an error message. `ATTRACTION STATUS` SHALL show the type.

#### Scenario: Create the four types
- **WHEN** `ATTRACTION CREATE;A01;Dragon Rojo;20;GENERAL`, `ATTRACTION CREATE;A02;Montana Rusa Individual;12;INDIVIDUAL`, `ATTRACTION CREATE;A03;Rafting Familiar;24;GROUP` and `ATTRACTION CREATE;A04;Pasaje del Terror;15;SPECIAL` are processed
- **THEN** the four attractions exist with those types

#### Scenario: Delivery 2 syntax creates GENERAL
- **WHEN** `ATTRACTION CREATE;A05;Noria;10` is processed
- **THEN** `A05` is a GENERAL attraction

#### Scenario: Unknown type rejected
- **WHEN** `ATTRACTION CREATE;A06;X;10;VIP` is processed
- **THEN** an error message is shown and `A06` is not created

### Requirement: Ticket types accepted by each attraction type
Normal entry SHALL be accepted only with ticket kinds allowed by the attraction type: GENERAL accepts individual and group tickets; INDIVIDUAL accepts only individual tickets; GROUP accepts only group tickets; SPECIAL accepts only individual tickets that have an unconsumed Special Pass. Other combinations SHALL be rejected with an error message and nobody SHALL enter.

#### Scenario: Group ticket at an individual attraction
- **WHEN** a valid group ticket is used to enter INDIVIDUAL attraction `A02`
- **THEN** an error message is shown and nobody enters

#### Scenario: Individual ticket at a group attraction
- **WHEN** a valid individual ticket is used to enter GROUP attraction `A03`
- **THEN** an error message is shown and nobody enters

#### Scenario: Special attraction requires a Special Pass
- **WHEN** an individual ticket without an unconsumed Special Pass is used at SPECIAL attraction `A04`, and then the same ticket after buying a Special Pass
- **THEN** the first entry is rejected and the second is accepted

#### Scenario: General attraction accepts both
- **WHEN** a valid individual ticket and a valid group ticket are used at GENERAL attraction `A01`
- **THEN** both entries are accepted

### Requirement: Access restrictions expressed with interfaces and generics
Ticket access capabilities SHALL be represented by interfaces implemented by individual and group tickets, and attractions SHALL be bounded generic types over those interfaces such that the generic parameter determines which tickets the attraction's entry operation accepts. Individual and special attractions SHALL use the same individual-ticket capability; the Special Pass condition SHALL be part of the special attraction's own behavior.

#### Scenario: Typed entry operation
- **WHEN** the attraction classes are inspected
- **THEN** each attraction type is parameterized with the access capability it accepts and its entry operation takes that capability type

### Requirement: Attraction type persisted
The attraction type SHALL be stored and reconstructed; stored attractions without a type SHALL be loaded as GENERAL.

#### Scenario: Restart keeps types
- **WHEN** attractions of the four types are saved and reloaded, together with a legacy line `A09;Vieja;5`
- **THEN** each keeps its type and `A09` is GENERAL

### Requirement: Individual entry with a SpeedPass
`ATTRACTION ENTER;<attraction>;<ticket>;SPEED` with an individual ticket at an attraction that accepts SpeedPasses SHALL require at least one unconsumed SpeedPass of that ticket, besides all normal entry conditions; on success exactly one of its unconsumed SpeedPasses SHALL be marked consumed and the output SHALL name it. If entry fails for any reason, no SpeedPass SHALL be modified.

#### Scenario: Entry consumes one SpeedPass
- **WHEN** `T001` (valid today) has unconsumed SpeedPasses `SP02:1`..`SP02:10` and `ATTRACTION ENTER;A01;T001;SPEED` is processed
- **THEN** the user enters, exactly one SpeedPass is consumed, and the output names it

#### Scenario: No SpeedPass available
- **WHEN** `T001` has no unconsumed SpeedPass and `ATTRACTION ENTER;A01;T001;SPEED` is processed
- **THEN** an error message is shown and nobody enters

#### Scenario: Failure leaves passes untouched
- **WHEN** `T001` has SpeedPasses but the attraction is full
- **THEN** entry is rejected and no SpeedPass is consumed

### Requirement: Group entry with SpeedPasses
`ATTRACTION ENTER;<attraction>;<groupTicket>;SPEED` at GENERAL and GROUP attractions SHALL check, before changing any state, that the group ticket is valid today, the attraction accepts group tickets, there is capacity for all members, and every composed individual ticket has an unconsumed SpeedPass. Entry and consumption SHALL be indivisible: on success all members enter and exactly one SpeedPass of each composed ticket is consumed; on any failure nobody enters and no SpeedPass is consumed.

#### Scenario: Whole group with SpeedPasses
- **WHEN** both composed tickets of `TG01` have an unconsumed SpeedPass and `ATTRACTION ENTER;A03;TG01;SPEED` is processed at GROUP attraction `A03`
- **THEN** both members enter and one SpeedPass of each composed ticket is consumed

#### Scenario: One member without SpeedPass
- **WHEN** only one composed ticket of `TG01` has an unconsumed SpeedPass
- **THEN** entry is rejected, nobody enters and no SpeedPass is consumed

### Requirement: Special Pass consumed on entry
Normal entry to a SPECIAL attraction SHALL automatically use the ticket's Special Pass, which SHALL be unconsumed, and SHALL mark it consumed only when entry succeeds, indicating this in the output. If the ticket has no Special Pass, it is already consumed, or another condition fails, entry SHALL be rejected without changing the pass. The `;SPEED` variant and group tickets SHALL be rejected at SPECIAL attractions.

#### Scenario: Special Pass consumed
- **WHEN** `T001` has unconsumed Special Pass `PE01` and `ATTRACTION ENTER;A04;T001` is processed at SPECIAL attraction `A04`
- **THEN** the user enters, `PE01` is consumed and the output says so

#### Scenario: Second entry with the consumed pass
- **WHEN** the user has left `A04` and `ATTRACTION ENTER;A04;T001` is processed again
- **THEN** entry is rejected because the Special Pass is consumed

#### Scenario: SPEED not allowed at special attractions
- **WHEN** `ATTRACTION ENTER;A04;T001;SPEED` is processed
- **THEN** an error message is shown, nobody enters and `PE01` is unchanged
