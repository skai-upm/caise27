# Spec Delta

## ADDED Requirements

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

## MODIFIED Requirements

### Requirement: Access through the common ticket abstraction
Attraction access SHALL be decided through the abstraction common to all ticket types, each ticket type providing the users it admits, rather than through separate access mechanisms per ticket type; the attraction SHALL have room for every admitted user or none SHALL enter.

#### Scenario: Users admitted by an individual ticket
- **WHEN** access is requested with an individual ticket
- **THEN** exactly the ticket's user is admitted

#### Scenario: Users admitted by a group ticket
- **WHEN** access is requested with a group ticket
- **THEN** exactly the users of its composition are admitted, through the same operation used for individual tickets
