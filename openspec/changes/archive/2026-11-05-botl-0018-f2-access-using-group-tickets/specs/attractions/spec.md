# Spec Delta

## ADDED Requirements

### Requirement: Group ticket access through the common abstraction
Attraction access SHALL determine who enters from the common ticket abstraction (the users associated with the ticket), with no separate access mechanism per ticket type. `ATTRACTION ENTER` with a group ticket valid on the current system date SHALL admit together all users of its composition who are not already inside, and only if the attraction has free capacity for all of them; otherwise it MUST print an `ERROR` response and nobody enters.

#### Scenario: Whole group enters
- **WHEN** group ticket `TG01` for today is composed of tickets for Ana and Pedro and attraction `A01` has 20 free places, and `ATTRACTION ENTER;A01;TG01` is processed
- **THEN** a response starting with `OK` is printed and both Ana and Pedro are inside `A01`

#### Scenario: Insufficient capacity for the whole group
- **WHEN** attraction `A02` has capacity 1 and `ATTRACTION ENTER;A02;TG01` is processed for a two-member group ticket
- **THEN** an `ERROR` response is printed and neither member is inside `A02`

#### Scenario: Group ticket not valid today
- **WHEN** `TG01` is dated on another day than the current system date
- **THEN** `ATTRACTION ENTER;A01;TG01` is rejected and nobody enters

### Requirement: Leaving an attraction
`ATTRACTION EXIT;AttractionIdentifier;Email` SHALL remove the user with that email from the attraction's occupants, freeing that place, and print a response starting with `OK`. An unknown attraction, an unknown email, or a user who is not inside MUST produce an `ERROR` response with the occupants unchanged. Users who entered with a group ticket SHALL leave individually with their own email.

#### Scenario: User leaves and frees capacity
- **WHEN** Ana is inside full attraction `A02` (capacity 1) and `ATTRACTION EXIT;A02;ana@correo.com` is processed
- **THEN** Ana is no longer inside and another valid ticket holder can enter `A02`

#### Scenario: Group members leave individually
- **WHEN** Ana and Pedro entered `A01` with group ticket `TG01` and `ATTRACTION EXIT;A01;ana@correo.com` is processed
- **THEN** Ana has left and Pedro is still inside

#### Scenario: Invalid exit
- **WHEN** `ATTRACTION EXIT;A99;ana@correo.com`, `ATTRACTION EXIT;A01;nadie@correo.com` or `ATTRACTION EXIT;A01;luis@correo.com` (Luis not inside) is processed
- **THEN** an `ERROR` response is printed and the occupants are unchanged

### Requirement: Attraction status
`ATTRACTION STATUS;AttractionIdentifier` SHALL print, from the attraction's own state, its identifier, name, maximum capacity, number of users currently inside, number of available places and the emails of the users inside, treating every occupant alike regardless of how they entered. It MUST NOT change the attraction. An unknown identifier MUST produce an `ERROR` response.

#### Scenario: Status after mixed entries
- **WHEN** attraction `A01` (capacity 20) contains Ana (entered with group ticket `TG01`) and Luis (entered with individual ticket `T003`)
- **THEN** `ATTRACTION STATUS;A01` shows `A01`, its name, capacity 20, 2 users inside, 18 available places, and both emails

#### Scenario: Status of an empty attraction
- **WHEN** attraction `A02` (capacity 12) is empty
- **THEN** `ATTRACTION STATUS;A02` shows 0 users inside and 12 available places

### Requirement: Exit and status listed in HELP
The `HELP` output SHALL list `ATTRACTION EXIT;<attraction>;<email>` and `ATTRACTION STATUS;<id>`.

#### Scenario: HELP shows exit and status
- **WHEN** `HELP` is processed
- **THEN** the output contains both syntaxes

## REMOVED Requirements

### Requirement: Access through the common ticket abstraction
**Reason**: It contained the temporary rule rejecting group tickets at entry, which this milestone lifts.
**Migration**: Replaced by "Group ticket access through the common abstraction", which keeps the common-abstraction rule and adds atomic group access.
