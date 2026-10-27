# Spec Delta

## ADDED Requirements

### Requirement: Entering with a group ticket
`ATTRACTION ENTER;AttractionIdentifier;TicketIdentifier` with a group ticket dated on the current system date SHALL let all users of the individual tickets in its composition into the attraction together, using the same access operation as individual tickets. Access SHALL be indivisible: if the available places are fewer than the users to enter, or any of them is already inside, none SHALL enter.

#### Scenario: Group enters together
- **WHEN** the system date is `2027-04-15`, `TG01` (dated `2027-04-15`) is composed of Ana's and Pedro's tickets, `A01` has 20 free places and `ATTRACTION ENTER;A01;TG01` is processed
- **THEN** a confirmation is printed and Ana and Pedro are both inside `A01` with 18 places available

#### Scenario: Insufficient capacity for the whole group
- **WHEN** attraction `A02` has capacity 1 and `ATTRACTION ENTER;A02;TG01` is processed for a group ticket of two users
- **THEN** an error about insufficient capacity is printed and nobody enters `A02`

#### Scenario: Group ticket for another date
- **WHEN** the system date is `2027-04-14` and `ATTRACTION ENTER;A01;TG01` is processed
- **THEN** an error is printed and nobody enters

### Requirement: Leaving an attraction
`ATTRACTION EXIT;AttractionIdentifier;Email` SHALL remove the user with that email from the attraction's users and release the corresponding place. Users who entered with a group ticket SHALL leave individually with their own email.

#### Scenario: One group member leaves
- **WHEN** Ana and Pedro entered `A01` with `TG01` and `ATTRACTION EXIT;A01;ana@correo.com` is processed
- **THEN** a confirmation is printed, Ana is no longer inside, Pedro still is, and one more place is available

#### Scenario: User not inside
- **WHEN** `ATTRACTION EXIT;A01;luis@correo.com` is processed and Luis is not inside `A01`
- **THEN** an error is printed and the attraction is unchanged

#### Scenario: Unknown attraction
- **WHEN** `ATTRACTION EXIT;A99;ana@correo.com` is processed
- **THEN** an error is printed

### Requirement: Attraction status
`ATTRACTION STATUS;AttractionIdentifier` SHALL display, from the attraction's own state, its identifier, name, maximum capacity, number of users currently inside, number of available places and the users inside, regardless of how they entered.

#### Scenario: Status after mixed entries
- **WHEN** `A01` (Dragon Rojo, capacity 20) has Ana and Pedro inside from a group ticket and Luis from an individual ticket, and `ATTRACTION STATUS;A01` is processed
- **THEN** the output shows `A01`, `Dragon Rojo`, capacity `20`, `3` users inside, `17` available places and the three users' emails

#### Scenario: Unknown attraction
- **WHEN** `ATTRACTION STATUS;A99` is processed
- **THEN** an error is printed
