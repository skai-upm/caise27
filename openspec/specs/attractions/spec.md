# attractions Specification

## Purpose

Defines park attractions with a limited capacity, how users enter them with tickets and leave them, and how their occupancy is consulted.

## Requirements

### Requirement: Attraction creation
`ATTRACTION CREATE;<Identifier>;<Name>;<Capacity>` SHALL create an empty attraction when the identifier is unused, the name is non-empty and the capacity is a positive integer.

#### Scenario: Create an attraction
- **WHEN** `ATTRACTION CREATE;A01;Dragon Rojo;20` is processed
- **THEN** attraction A01 exists with capacity 20 and no occupants

#### Scenario: Invalid capacity
- **WHEN** `ATTRACTION CREATE;A03;Noria;0` or `ATTRACTION CREATE;A03;Noria;abc` is processed
- **THEN** an error is printed and no attraction is created

### Requirement: Entering with a ticket
`ATTRACTION ENTER;<Attraction>;<Ticket>` SHALL admit the users of an existing ticket whose date equals the system date: the ticket's user for an individual ticket, or every user of its individual tickets for a group ticket. Entry SHALL be all-or-nothing: if the free places are fewer than the users to admit, or any of them is already inside an attraction, nobody enters. Later date changes SHALL NOT affect users already inside.

#### Scenario: Individual entry
- **WHEN** `ATTRACTION ENTER;A02;T001` is processed and T001 is Ana's ticket for the system date
- **THEN** Ana is inside A02

#### Scenario: Group entry
- **WHEN** `ATTRACTION ENTER;A01;TG01` is processed and TG01 is a three-member group ticket for the system date
- **THEN** the three members are inside A01

#### Scenario: Not enough capacity
- **WHEN** a three-member group ticket is used on an attraction with two free places
- **THEN** an error is printed and nobody enters

#### Scenario: Ticket for another date
- **WHEN** `ATTRACTION ENTER` uses a ticket whose date is not the system date
- **THEN** an error is printed and nobody enters

### Requirement: Leaving an attraction
`ATTRACTION EXIT;<Attraction>;<Email>` SHALL remove the user with that email from the attraction's occupants, releasing a place; users who entered with a group ticket SHALL leave individually.

#### Scenario: Leave
- **WHEN** `ATTRACTION EXIT;A01;ana@correo.com` is processed and Ana is inside A01
- **THEN** Ana is no longer inside A01 and one more place is free

#### Scenario: User not inside
- **WHEN** `ATTRACTION EXIT` names a user who is not inside the attraction
- **THEN** an error is printed

### Requirement: Listing occupants
`ATTRACTION LIST;<Attraction>` SHALL print the email of every user currently inside, or an explicit empty result, without changing the attraction.

#### Scenario: Empty attraction
- **WHEN** `ATTRACTION LIST;A02` is processed and nobody is inside
- **THEN** an empty result is printed

### Requirement: Attraction status
`ATTRACTION STATUS;<Attraction>` SHALL print the identifier, name, maximum capacity, number of users inside, number of free places and the users inside, regardless of how they entered.

#### Scenario: Status after group entry
- **WHEN** three users entered A01 (capacity 20) with a group ticket and `ATTRACTION STATUS;A01` is processed
- **THEN** the output shows A01, Dragon Rojo, capacity 20, 3 inside, 17 free and the three emails
