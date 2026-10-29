# Spec Delta

## MODIFIED Requirements

### Requirement: Attraction creation
`ATTRACTION CREATE;Identifier;Name;Capacity;Type` SHALL create an attraction of type GENERAL, INDIVIDUAL, GROUP or SPECIAL with a unique identifier, a name, a maximum capacity (positive integer) and an empty occupant collection; the form without type creates a GENERAL attraction. Duplicate or empty identifiers, invalid capacities and unknown types MUST be rejected.

#### Scenario: Create attraction
- **WHEN** `ATTRACTION CREATE;A01;Dragon Rojo;20` is processed
- **THEN** A01 named `Dragon Rojo` with capacity 20, type GENERAL and no occupants exists

#### Scenario: Create typed attraction
- **WHEN** `ATTRACTION CREATE;A04;Pasaje del Terror;15;SPECIAL` is processed
- **THEN** A04 of type SPECIAL exists

#### Scenario: Invalid capacity
- **WHEN** `ATTRACTION CREATE;A03;Noria;0` or `ATTRACTION CREATE;A03;Noria;diez` is processed
- **THEN** an error is shown and no attraction is created

#### Scenario: Unknown type
- **WHEN** `ATTRACTION CREATE;A05;Noria;10;VIP` is processed
- **THEN** an error is shown and no attraction is created

### Requirement: Entering with a ticket
`ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier` (optionally followed by `;SPEED`) SHALL admit the users of an existing ticket whose date equals the current system date and whose type the attraction accepts: the ticket's user for an individual ticket, or every user in the composition for a group ticket. The same operation MUST handle all ticket types through common access abstractions.

#### Scenario: Individual entry
- **WHEN** the date is 2027-04-15 and `ATTRACTION ENTER;A01;T001` is processed for Ana's ticket dated 2027-04-15
- **THEN** Ana is inside A01

#### Scenario: Group entry
- **WHEN** `ATTRACTION ENTER;A01;TG01` is processed for a valid group ticket with Ana and Pedro
- **THEN** Ana and Pedro are inside A01

#### Scenario: Ticket not valid today
- **WHEN** the ticket's date differs from the current system date, or the ticket does not exist
- **THEN** an error is shown and nobody enters

#### Scenario: Presence independent of later date changes
- **WHEN** Ana entered A01 and the system date is changed afterwards
- **THEN** Ana is still inside A01

## ADDED Requirements

### Requirement: Access by attraction type
GENERAL attractions SHALL accept individual and group tickets, INDIVIDUAL attractions individual tickets only, GROUP attractions group tickets only, and SPECIAL attractions individual tickets only. The restriction MUST be expressed with access interfaces implemented by the tickets and bounded generic attraction types.

#### Scenario: Group ticket at individual attraction
- **WHEN** `ATTRACTION ENTER;A02;TG01` is processed for INDIVIDUAL attraction A02
- **THEN** an error is shown and nobody enters

#### Scenario: Individual ticket at group attraction
- **WHEN** `ATTRACTION ENTER;A03;T001` is processed for GROUP attraction A03
- **THEN** an error is shown and nobody enters

### Requirement: Entry with SpeedPass
With `;SPEED`, at GENERAL, INDIVIDUAL and GROUP attractions every individual ticket entering (the ticket itself or each composition ticket) SHALL have an unconsumed SpeedPass; on success exactly one SpeedPass per ticket is marked consumed and the output names the consumed passes. If any check (date, type, capacity, passes) fails, nobody enters and no pass changes.

#### Scenario: Individual speed entry
- **WHEN** `ATTRACTION ENTER;A01;T001;SPEED` is processed and T001 has unconsumed SpeedPasses SP01:1 and SP02:1
- **THEN** Ana enters, exactly one of them is consumed and the output names it

#### Scenario: Group speed entry is atomic
- **WHEN** `ATTRACTION ENTER;A01;TG01;SPEED` is processed and one composition ticket has no unconsumed SpeedPass
- **THEN** no member enters and no SpeedPass is consumed

#### Scenario: Speed without passes
- **WHEN** `ATTRACTION ENTER;A01;T001;SPEED` is processed and T001 has no unconsumed SpeedPass
- **THEN** an error is shown and Ana does not enter

### Requirement: Entry to special attractions
At a SPECIAL attraction the normal syntax SHALL automatically use the ticket's Special Pass, which must exist and be unconsumed; it is marked consumed only when entry succeeds and the output says so. The `;SPEED` variant and group tickets MUST be rejected there.

#### Scenario: Special entry
- **WHEN** `ATTRACTION ENTER;A04;T001` is processed and T001 has unconsumed Special Pass PE01
- **THEN** Ana enters, PE01 is consumed and the output reports it

#### Scenario: Special pass missing or consumed
- **WHEN** T001 has no Special Pass or it is consumed
- **THEN** entry is rejected and the pass state is unchanged

#### Scenario: Speed not allowed at special
- **WHEN** `ATTRACTION ENTER;A04;T001;SPEED` is processed
- **THEN** an error is shown and no pass changes
