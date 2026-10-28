# Spec Delta

## ADDED Requirements

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
