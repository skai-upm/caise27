# Spec Delta

## Purpose

Models the park's attractions, their capacity and the users currently inside them, and lets ticket holders enter and leave.

## ADDED Requirements

### Requirement: Attraction creation
`ATTRACTION CREATE;Identifier;Name;Capacity` SHALL create an attraction with a non-empty identifier unique among attractions (ignoring case), a non-empty name and a positive integer capacity, initially with no users inside.

#### Scenario: Create attraction
- **WHEN** `ATTRACTION CREATE;A01;Dragon Rojo;20` is processed
- **THEN** `ATTRACTION CREATED` is printed and A01 exists with capacity 20 and no occupants

#### Scenario: Invalid capacity or duplicate
- **WHEN** the capacity is `0`, `-5` or `abc`, or an attraction with that identifier exists
- **THEN** an error is printed and no attraction is created

### Requirement: Entering an attraction
`ATTRACTION ENTER;Attraction;Ticket` SHALL let the ticket's holders enter when the attraction and ticket exist and the ticket's date equals the current system date. An individual ticket admits its user; a group ticket admits every user of its member tickets. Entry SHALL be all-or-nothing: if the free places are fewer than the people entering, or any of them is already inside an attraction, nobody enters. Later date changes SHALL NOT affect users already inside.

#### Scenario: Individual entry
- **WHEN** T001 is Ana's ticket for today and `ATTRACTION ENTER;A02;T001` is processed
- **THEN** a confirmation is printed and Ana is inside A02

#### Scenario: Group entry
- **WHEN** TG01 has three member tickets for today and `ATTRACTION ENTER;A01;TG01` is processed with 20 free places
- **THEN** all three users are inside A01

#### Scenario: Insufficient capacity
- **WHEN** an attraction has 2 free places and a group ticket with 3 members is used
- **THEN** an error is printed and none of the three users enters

#### Scenario: Ticket not valid today
- **WHEN** the ticket's date differs from the system date
- **THEN** an error is printed and nobody enters

#### Scenario: Unknown ticket or attraction
- **WHEN** the ticket or the attraction does not exist
- **THEN** an error is printed

### Requirement: Leaving an attraction
`ATTRACTION EXIT;Attraction;Email` SHALL remove the user with that email from the attraction's occupants and free their place, regardless of how they entered. A user not inside SHALL produce an error.

#### Scenario: Leave
- **WHEN** Ana is inside A01 and `ATTRACTION EXIT;A01;ana@correo.com` is processed
- **THEN** a confirmation is printed and A01 has one more free place

#### Scenario: Not inside
- **WHEN** Ana is not inside A01 and `ATTRACTION EXIT;A01;ana@correo.com` is processed
- **THEN** an error is printed

### Requirement: Listing occupants
`ATTRACTION LIST;Attraction` SHALL print the email of every user currently inside, or a message that it is empty, without changing the attraction.

#### Scenario: List occupants
- **WHEN** Ana and Pedro are inside A01 and `ATTRACTION LIST;A01` is processed
- **THEN** `ana@correo.com` and `pedro@correo.com` are printed

#### Scenario: Empty attraction
- **WHEN** nobody is inside A02 and `ATTRACTION LIST;A02` is processed
- **THEN** a message that the attraction is empty is printed

### Requirement: Attraction status
`ATTRACTION STATUS;Attraction` SHALL print the identifier, name, maximum capacity, number of users inside, number of free places and the users inside.

#### Scenario: Status
- **WHEN** three users are inside A01 (capacity 20) and `ATTRACTION STATUS;A01` is processed
- **THEN** the output shows A01, `Dragon Rojo`, capacity 20, 3 inside, 17 available, and the three users
