# attraction-access Specification

## Purpose
Defines how users access attractions with tickets: the ticket must exist and be valid for the current system date, the attraction must have enough capacity, and the users of the ticket become occupants of the attraction.

## Requirements

### Requirement: ATTRACTION ENTER with an individual ticket
The command `ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier` with an existing individual ticket dated on the current system date SHALL add the ticket's user to the attraction's occupants and print `USER ENTERED ATTRACTION: <email>`.

#### Scenario: Valid entry
- **WHEN** the current date is 2027-04-15, Ana has ticket `T001` dated 2027-04-15, attraction `A01` has room, and `ATTRACTION ENTER;A01;T001` is processed
- **THEN** `USER ENTERED ATTRACTION: ana@correo.com` is printed and Ana is listed by `ATTRACTION LIST;A01`

### Requirement: Ticket valid for the current date
Entry SHALL be rejected with an `ERROR:` message if the attraction does not exist, the ticket does not exist, or the ticket's date is not the current system date. The ticket date SHALL be checked only at entry: a user already inside SHALL remain inside when the system date changes afterwards.

#### Scenario: Ticket for another day
- **WHEN** the current date is 2027-04-15 and `ATTRACTION ENTER;A01;T002` is processed for a ticket dated 2027-04-16
- **THEN** an `ERROR:` line about the ticket date is printed and nobody enters

#### Scenario: Presence survives a date change
- **WHEN** Ana entered `A01` with a ticket for 2027-04-15 and then `SYSTEM DATE;2027-04-16` is processed
- **THEN** Ana is still listed by `ATTRACTION LIST;A01`

#### Scenario: Unknown ticket or attraction
- **WHEN** `ATTRACTION ENTER;A01;T999` or `ATTRACTION ENTER;A99;T001` is processed and that ticket or attraction does not exist
- **THEN** an `ERROR:` line is printed and nobody enters

### Requirement: Capacity limit
Entry SHALL be rejected with an `ERROR:` message when the attraction does not have room for every user entering with the ticket; in that case nobody SHALL enter.

#### Scenario: Full attraction
- **WHEN** attraction `A03` has capacity 1 and already contains one user, and another user with a valid ticket tries to enter
- **THEN** an `ERROR:` line about capacity is printed and the occupants of `A03` are unchanged

### Requirement: No duplicate presence
A user already inside an attraction SHALL NOT enter it again; such an entry SHALL be rejected with an `ERROR:` message.

#### Scenario: Enter twice
- **WHEN** Ana is inside `A01` and `ATTRACTION ENTER;A01;T001` is processed again
- **THEN** an `ERROR:` line is printed and Ana appears once in `A01`

### Requirement: Single access mechanism
Access SHALL be implemented once on the common ticket abstraction: the users entering are those of the ticket's individual tickets, so every ticket type uses the same capacity and presence checks.

#### Scenario: Common mechanism
- **WHEN** the access operation is given any ticket type
- **THEN** it determines the entering users from the ticket's individual tickets and applies the same checks

### Requirement: ATTRACTION ENTER with a group ticket
`ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier` with a group ticket valid for the current system date SHALL let every user of the group ticket's individual tickets enter the attraction together, printing one `USER ENTERED ATTRACTION: <email>` line per user. Entry with a group ticket SHALL be indivisible: if the attraction lacks room for all of them, or any of them is already inside, nobody SHALL enter and an `ERROR:` message SHALL be printed.

#### Scenario: Group enters together
- **WHEN** group ticket `TG01` for 2027-04-15 includes Ana and Pedro, the current date is 2027-04-15, `A01` has room, and `ATTRACTION ENTER;A01;TG01` is processed
- **THEN** Ana and Pedro are both inside `A01`

#### Scenario: Not enough room for the whole group
- **WHEN** `A02` has 1 free place and `ATTRACTION ENTER;A02;TG01` is processed for a group ticket with two users
- **THEN** an `ERROR:` line about capacity is printed and neither user enters

### Requirement: ATTRACTION EXIT command
The command `ATTRACTION EXIT;AttractionIdentifier;Email` SHALL remove the user with that email from the attraction's occupants, freeing one place, and print `USER LEFT ATTRACTION: <email>`. Users who entered with a group ticket SHALL leave individually. An unknown attraction or an email of no user inside SHALL be rejected with an `ERROR:` message.

#### Scenario: One group member leaves
- **WHEN** Ana and Pedro entered `A01` with `TG01` and `ATTRACTION EXIT;A01;ana@correo.com` is processed
- **THEN** `USER LEFT ATTRACTION: ana@correo.com` is printed, Pedro is still inside and one more place is available

#### Scenario: User not inside
- **WHEN** `ATTRACTION EXIT;A01;luis@correo.com` is processed and Luis is not inside `A01`
- **THEN** an `ERROR:` line is printed and the occupants are unchanged

### Requirement: ATTRACTION STATUS command
The command `ATTRACTION STATUS;AttractionIdentifier` SHALL print, from the attraction's own state, its identifier, name, maximum capacity, number of users currently inside, number of available places and the users currently inside, treating all occupants alike however they entered. It SHALL NOT change the attraction; an unknown identifier SHALL be rejected with an `ERROR:` message.

#### Scenario: Status after mixed entries
- **WHEN** `A01` (Dragon Rojo, capacity 20) contains Luis (individual ticket) and Pedro (group ticket) and `ATTRACTION STATUS;A01` is processed
- **THEN** the output shows `A01`, `Dragon Rojo`, capacity 20, 2 occupants, 18 available places and both users' emails
